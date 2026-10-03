package com.leaf.techjs.jei;

import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * JEI 相关操作。本类只能在 CompatMods.JEI.isLoaded() 为 true 时引用，
 * 否则会因 JEI 类缺失而 NoClassDefFoundError
 */
public final class TechJEI {

    private TechJEI() {
    }

    /** 科技启用：向 JEI 添加配方并解除隐藏 */
    public static void addRecipes(List<Recipe<?>> recipes) {
        IJeiRuntime runtime = TechSystemJEI.jeiRuntime;
        if (runtime == null || recipes.isEmpty()) return;

        forEachType(runtime.getRecipeManager(), recipes, TechJEI::addRecipesByType);
    }

    /** 科技禁用：把配方从 JEI 中隐藏 */
    public static void removeRecipes(List<Recipe<?>> recipes) {
        IJeiRuntime runtime = TechSystemJEI.jeiRuntime;
        if (runtime == null || recipes.isEmpty()) return;

        forEachType(runtime.getRecipeManager(), recipes, TechJEI::hideRecipesByType);
    }

    @SuppressWarnings("unchecked")
    private static void addRecipesByType(RecipeType<?> type, List<?> list) {
        IRecipeManager manager = TechSystemJEI.jeiRuntime.getRecipeManager();
        manager.addRecipes((RecipeType<Object>) type, (List<Object>) list);
        manager.unhideRecipes((RecipeType<Object>) type, (List<Object>) list);
    }

    @SuppressWarnings("unchecked")
    private static void hideRecipesByType(RecipeType<?> type, List<?> list) {
        TechSystemJEI.jeiRuntime.getRecipeManager().hideRecipes((RecipeType<Object>) type, (List<Object>) list);
    }

    /** 按配方的 RecipeType 分组，找到 JEI 对应的 RecipeType 后执行操作 */
    private static void forEachType(IRecipeManager manager, List<Recipe<?>> recipes,
                                    BiConsumer<RecipeType<?>, List<Object>> action) {
        Map<ResourceLocation, List<Object>> byType = new LinkedHashMap<>();
        for (Recipe<?> recipe : recipes) {
            ResourceLocation uid = ForgeRegistries.RECIPE_TYPES.getKey(recipe.getType());
            if (uid == null) continue;
            byType.computeIfAbsent(uid, k -> new ArrayList<>()).add(recipe);
        }

        byType.forEach((uid, list) ->
                manager.getRecipeType(uid).ifPresent(type -> action.accept(type, list)));
    }
}
