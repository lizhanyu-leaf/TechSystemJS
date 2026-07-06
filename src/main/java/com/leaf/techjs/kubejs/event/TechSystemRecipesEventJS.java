package com.leaf.techjs.kubejs.event;

import com.google.common.base.Stopwatch;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.leaf.techjs.context.TechSystemStorage;
import com.leaf.techjs.kubejs.TechSystemEvents;
import com.leaf.techjs.mixin.RecipeManagerAccessor;
import com.leaf.techjs.mixin.ReloadableServerResourcesAccessor;
import dev.latvian.mods.kubejs.DevProperties;
import dev.latvian.mods.kubejs.core.ItemStackKJS;
import dev.latvian.mods.kubejs.item.ingredient.TagContext;
import dev.latvian.mods.kubejs.platform.RecipePlatformHelper;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import dev.latvian.mods.kubejs.recipe.schema.JsonRecipeSchema;
import dev.latvian.mods.kubejs.recipe.special.SpecialRecipeSerializerManager;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.server.DataExport;
import dev.latvian.mods.kubejs.server.KubeJSReloadListener;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.kubejs.util.KubeJSPlugins;
import dev.latvian.mods.kubejs.util.UtilsJS;
import dev.latvian.mods.rhino.mod.util.JsonUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TechSystemRecipesEventJS extends RecipesEventJS {
    private static Map<ResourceLocation, JsonElement> datapackRecipeMap;
    private static Map<ResourceLocation, RecipeJS> originalRecipesCache;
    private boolean shouldDatapackUpdate = false;

    // 需要用到的父类的 private 字段

    private static final Predicate<RecipeJS> RECIPE_NOT_REMOVED = r -> r != null && !r.removed;
    private static final Function<Recipe<?>, ResourceLocation> RECIPE_ID = Recipe::getId;
    private static final Predicate<Recipe<?>> RECIPE_NON_NULL = Objects::nonNull;
    private static final Function<Recipe<?>, Recipe<?>> RECIPE_IDENTITY = Function.identity();

    private static final BinaryOperator<Recipe<?>> MERGE_ORIGINAL = (a, b) -> {
        ConsoleJS.SERVER.warn("Duplicate original recipe for id " + a.getId() + "!\nRecipe A: " + recipeToString(a) + "\nRecipe B: " + recipeToString(b) + "\nUsing last one encountered.");
        return b;
    };

    private static final BinaryOperator<Recipe<?>> MERGE_ADDED = (a, b) -> {
        ConsoleJS.SERVER.error("Duplicate added recipe for id " + a.getId() + "!\nRecipe A: " + recipeToString(a) + "\nRecipe B: " + recipeToString(b) + "\nUsing last one encountered.");
        return b;
    };

    /**
     * 设置 datapackRecipeMap 缓存
     * 为了处理在两次Reload中间时获取不到数据包配方的情况
     * @param datapackRecipeMap 配方数据
     */
    public void setDatapackRecipeMap(Map<ResourceLocation, JsonElement> datapackRecipeMap) {
        TechSystemRecipesEventJS.datapackRecipeMap = datapackRecipeMap;
        shouldDatapackUpdate = true;
    }

    /**
     * 执行 TechnologyEvents.onTechnologyLoad 的JS代码 并 替换原有配方
     * @param recipeManager RecipeManager 实例，获取和替换的目标
     */
    @SuppressWarnings("UnstableApiUsage")
    public void post(RecipeManager recipeManager) {
        ConsoleJS.SERVER.info("Processing Technology recipes...");

        // 设置TagContext
        TagContext.INSTANCE.setValue(TagContext.fromLoadResult(
                ((ReloadableServerResourcesAccessor)KubeJSReloadListener.resources).getTagManager().getResult()));

        // 清空更改配配方
        RecipesEventJS.MODIFY_RESULT_CALLBACKS.clear();

        var timer = Stopwatch.createStarted();

        // 为datapack添加的配方进行计时
        if (shouldDatapackUpdate) {
            var exportedRecipes = new JsonObject();

            {
                for (var entry : datapackRecipeMap.entrySet()) {
                    var recipeId = entry.getKey();

                    var recipeIdAndType = recipeId + "[unknown:type]";
                    JsonObject json;

                    try {
                        if (recipeId == null || (recipeId.getPath().startsWith("_"))) {
                            continue; //Forge: filter anything beginning with "_" as it's used for metadata.
                        }

                        json = RecipePlatformHelper.get().checkConditions(GsonHelper.convertToJsonObject(entry.getValue(), "top element"));

                        if (json == null) {
                            if (DevProperties.get().logSkippedRecipes) {
                                ConsoleJS.SERVER.info("Skipping recipe " + recipeId + ", conditions not met");
                            }

                            continue;
                        } else if (!json.has("type")) {
                            if (DevProperties.get().logSkippedRecipes) {
                                ConsoleJS.SERVER.info("Skipping recipe " + recipeId + ", missing type");
                            }

                            continue;
                        }

                        if (DataExport.export != null) {
                            exportedRecipes.add(recipeId.toString(), JsonUtils.copy(json));
                        }
                    } catch (Exception ex) {
                        if (DevProperties.get().logSkippedRecipes) {
                            ConsoleJS.SERVER.warn("Skipping recipe %s, failed to load: ".formatted(recipeId), ex);
                        }
                        continue;
                    }

                    var typeStr = GsonHelper.getAsString(json, "type");
                    recipeIdAndType = recipeId + "[" + typeStr + "]";
                    var type = getRecipeFunction(typeStr);

                    if (type == null) {
                        if (DevProperties.get().logSkippedRecipes) {
                            ConsoleJS.SERVER.info("Skipping recipe " + recipeId + ", unknown type: " + typeStr);
                        }

                        continue;
                    }

                    try {
                        var recipe = type.schemaType.schema.deserialize(type, recipeId, json);
                        recipe.afterLoaded();
                        originalRecipes.put(recipeId, recipe);

                        if (ConsoleJS.SERVER.shouldPrintDebug()) {
                            var originalRecipe = recipe.getOriginalRecipe();
                            if (originalRecipe == null || SpecialRecipeSerializerManager.INSTANCE.isSpecial(originalRecipe)) {
                                ConsoleJS.SERVER.debug("Loaded recipe " + recipeIdAndType + ": <dynamic>");
                            } else {
                                ConsoleJS.SERVER.debug("Loaded recipe " + recipeIdAndType + ": " + recipe.getFromToString());
                            }
                        }
                    } catch (Throwable ex) {
                        if (DevProperties.get().logErroringRecipes || DevProperties.get().debugInfo) {
                            ConsoleJS.SERVER.warn("Failed to parse recipe '" + recipeIdAndType + "'! Falling back to vanilla", ex, SKIP_ERROR);
                        }

                        try {
                            originalRecipes.put(recipeId, JsonRecipeSchema.SCHEMA.deserialize(type, recipeId, json));
                        } catch (NullPointerException | IllegalArgumentException | JsonParseException ex2) {
                            if (DevProperties.get().logErroringRecipes || DevProperties.get().debugInfo) {
                                ConsoleJS.SERVER.warn("Failed to parse recipe " + recipeIdAndType, ex2, SKIP_ERROR);
                            }
                        } catch (Exception ex3) {
                            ConsoleJS.SERVER.warn("Failed to parse recipe " + recipeIdAndType, ex3, SKIP_ERROR);
                        }
                    }
                }
            }

            ConsoleJS.SERVER.info("Found " + originalRecipes.size() + " recipes in " + timer.stop());

            // 释放计时器
            timer.reset().start();

            originalRecipesCache = originalRecipes;
        }
        else {
            originalRecipes.clear();
            originalRecipes.putAll(originalRecipesCache);
        }

        // 调用OnTechnologyLoad事件的Listener
        // TechnologyEvents.ON_TECHNOLOGY_LOAD.post(ScriptType.SERVER, this);

        // 调用已启用的 OnTechnologyLoad 事件的 Listener
        if (TechSystemStorage.getInstance() != null) {
            for (var techInfo : TechSystemStorage.getInstance().getAllActive()) {
                // 判断是否有监听，如果没有监听则跳过
                if (!TechSystemEvents.ON_TECHNOLOGY_LOAD.hasListeners(techInfo)) continue;
                // 核心逻辑，post 用于触发JS代码，加载JS配方
                TechSystemEvents.ON_TECHNOLOGY_LOAD
                        .post(ScriptType.SERVER, techInfo, this);
            }
        }

        int modifiedCount = 0;
        var removedRecipes = new ConcurrentLinkedQueue<RecipeJS>();

        // 处理删除配方
        for (var r : originalRecipes.values()) {
            if (r.removed) {
                removedRecipes.add(r);
            } else if (r.hasChanged()) {
                modifiedCount++;
            }
        }

        ConsoleJS.SERVER.info("Posted recipe events in " + timer.stop());

        timer.reset().start();
        // 处理删除添加配方
        addedRecipes.removeIf(TechSystemRecipesEventJS::addedRecipeRemoveCheck);

        var recipesByName = new HashMap<ResourceLocation, Recipe<?>>(originalRecipes.size() + addedRecipes.size());

        // 为 原版的 datapack 方式添加配方
        try {
            recipesByName.putAll(runInParallel(() -> originalRecipes.values().parallelStream()
                    .filter(RECIPE_NOT_REMOVED)
                    .map(this::createRecipe)
                    .filter(RECIPE_NON_NULL)
                    .collect(Collectors.toConcurrentMap(RECIPE_ID, RECIPE_IDENTITY, MERGE_ORIGINAL))));
        } catch (Throwable ex) {
            ConsoleJS.SERVER.error("Error creating datapack recipes", ex, SKIP_ERROR);
        }

        // 为JS脚本的方式添加配方
        try {
            recipesByName.putAll(runInParallel(() -> addedRecipes.parallelStream()
                    .map(this::createRecipe)
                    .filter(RECIPE_NON_NULL)
                    .collect(Collectors.toConcurrentMap(RECIPE_ID, RECIPE_IDENTITY, MERGE_ADDED))));
        } catch (Throwable ex) {
            ConsoleJS.SERVER.error("Error creating script recipes", ex, SKIP_ERROR);
        }

        KubeJSPlugins.forEachPlugin(p -> p.injectRuntimeRecipes(this, recipeManager, recipesByName));

        // 创建 RecipeMap 覆盖原版
        var newRecipeMap = new HashMap<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>();

        for (var entry : recipesByName.entrySet()) {
            var type = entry.getValue().getType();
            var recipes = newRecipeMap.computeIfAbsent(type, k -> new HashMap<>());
            recipes.put(entry.getKey(), entry.getValue());
        }

        // 覆盖配方
        ((RecipeManagerAccessor)recipeManager).setByName(recipesByName);
        ((RecipeManagerAccessor)recipeManager).setRecipes(newRecipeMap);
        ConsoleJS.SERVER.info("Added " + addedRecipes.size() + " recipes, removed " + removedRecipes.size() + " recipes, modified " + modifiedCount + " recipes, with " + failedCount.get() + " failed recipes in " + timer.stop());
        RecipeJS.itemErrors = false;

        if (DataExport.export != null) {
            for (var r : removedRecipes) {
                DataExport.export.addJson("removed_recipes/" + r.getId() + ".json", r.json);
            }
        }
    }

    @Override
    public RecipeJS addRecipe(RecipeJS r, boolean json) {
        return super.addRecipe(r, json);
    }

    // 搬运一下需要用到的父类的 private 方法
    @Nullable
    private Recipe<?> createRecipe(RecipeJS r) {
        try {
            var rec = r.createRecipe();
            var path = r.kjs$getMod() + "/" + r.getPath();

            if (!r.removed && DataExport.export != null) {
                DataExport.export.addJson("recipes/%s.json".formatted(path), r.json);

                if (r.newRecipe) {
                    DataExport.export.addJson("added_recipes/%s.json".formatted(path), r.json);
                }
            }

            return rec;
        } catch (Throwable ex) {
            ConsoleJS.SERVER.warn("Error parsing recipe " + r + ": " + r.json, ex, SKIP_ERROR);
            failedCount.incrementAndGet();
            return null;
        }
    }

    private static boolean addedRecipeRemoveCheck(RecipeJS r) {
        // r.getOrCreateId(); // Generate ID synchronously?
        return !r.newRecipe;
    }

    @SuppressWarnings("DataFlowIssue")
    private static String recipeToString(Recipe<?> recipe) {
        var map = new LinkedHashMap<String, Object>();
        map.put("type", RegistryInfo.RECIPE_SERIALIZER.getId(recipe.getSerializer()));

        try {
            var in = new ArrayList<>();

            for (var ingredient : recipe.getIngredients()) {
                var list = new ArrayList<String>();

                for (var item : ingredient.getItems()) {
                    list.add(((ItemStackKJS) (Object) item).kjs$toItemString());
                }

                in.add(list);
            }

            map.put("in", in);
        } catch (Exception ex) {
            map.put("in_error", ex.toString());
        }

        try {
            var result = recipe.getResultItem(UtilsJS.staticRegistryAccess);
            //noinspection ConstantValue
            map.put("out", ((ItemStackKJS) (Object) (result == null ? ItemStack.EMPTY : result)).kjs$toItemString());
        } catch (Exception ex) {
            map.put("out_error", ex.toString());
        }

        return map.toString();
    }
}
