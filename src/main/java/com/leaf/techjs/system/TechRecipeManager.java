package com.leaf.techjs.system;

import com.leaf.techjs.AllPackets;
import com.leaf.techjs.foundation.ServerEvents;
import com.leaf.techjs.foundation.UpdateTechPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class TechRecipeManager {
    /** 带科技标记的配方（由 RecipesEventJSMixin 在 RecipesEventJS.post 结束时收集），tech id -> 配方列表 */
    static final Map<ResourceLocation, List<Recipe<?>>> techRecipes = new HashMap<>();

    /**
     * RecipesEventJSMixin 在 RecipesEventJS.post 开始时调用：
     * 脚本即将重跑，清空旧配方，随后会按当前启用状态重新收集
     */
    public static void reset() {
        techRecipes.clear();
    }

    /**
     * RecipesEventJSMixin 在 RecipesEventJS.post 结束时调用（此时配方已随 addedRecipes 合并进配方管理器）：
     * <ul>
     *     <li>科技未启用：从配方管理器中移除，并应从 addedRecipes 中删除</li>
     *     <li>科技已启用：保持原样，随 post 正常进入配方管理器（reload 后由原版配方包强制双端同步）</li>
     * </ul>
     *
     * @return 配方是否应从 addedRecipes 中移除（即科技未启用）
     */
    public static boolean divert(ResourceLocation techId, Recipe<?> recipe, RecipeManager manager) {
        techRecipes.computeIfAbsent(techId, k -> new ArrayList<>()).add(recipe);
        if (TechManager.isEnable(techId)) {
            return false;
        }
        removeFromManager(manager, recipe);
        return true;
    }

    /**
     * 科技启停时调用（TechManager.toggle）：更新配方管理器并同步客户端
     */
    static void update(ResourceLocation id) {
        if (ServerEvents.server == null) return;

        apply(id);

        // 同步客户端：下发配方 + 科技状态（客户端再据此更新 JEI、触发 client script 事件）
        RecipeManager manager = ServerEvents.server.getRecipeManager();
        ClientboundUpdateRecipesPacket recipePacket = new ClientboundUpdateRecipesPacket(manager.getRecipes());
        UpdateTechPacket techPacket = new UpdateTechPacket(
                id, TechManager.isEnable(id), false, getRecipeIds(id), techRecipes.getOrDefault(id, List.of()));

        for (ServerPlayer player : ServerEvents.server.getPlayerList().getPlayers()) {
            player.connection.send(recipePacket);
            AllPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> player), techPacket);
        }
    }

    /** 某个科技的配方 id 列表（客户端同步用） */
    public static List<ResourceLocation> getRecipeIds(ResourceLocation id) {
        return techRecipes.getOrDefault(id, List.of()).stream().map(Recipe::getId).toList();
    }

    /**
     * 服务器启动完成时调用：把所有科技的当前状态幂等地应用到配方管理器。
     * 用于兜底纠正配方事件（divert）先于状态加载运行的时序
     */
    public static void applyAll() {
        if (ServerEvents.server == null) return;
        for (ResourceLocation id : techRecipes.keySet()) {
            apply(id);
        }
    }

    /** 把某个科技当前的启用状态应用到配方管理器（不发包） */
    private static void apply(ResourceLocation id) {
        List<Recipe<?>> recipes = techRecipes.get(id);
        if (recipes == null || recipes.isEmpty()) return;

        assert ServerEvents.server != null;
        RecipeManager manager = ServerEvents.server.getRecipeManager();
        Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> map = mutableRecipes(manager);
        Map<ResourceLocation, Recipe<?>> byName = mutableByName(manager);

        if (TechManager.isEnable(id)) {
            recipes.forEach(recipe -> {
                map.computeIfAbsent(recipe.getType(), k -> new HashMap<>()).put(recipe.getId(), recipe);
                byName.put(recipe.getId(), recipe);
            });
        } else {
            recipes.forEach(recipe -> {
                Map<ResourceLocation, Recipe<?>> bucket = map.get(recipe.getType());
                if (bucket != null) bucket.remove(recipe.getId());
                byName.remove(recipe.getId());
            });
        }
    }

    private static void removeFromManager(RecipeManager manager, Recipe<?> recipe) {
        Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> map = mutableRecipes(manager);
        Map<ResourceLocation, Recipe<?>> byName = mutableByName(manager);

        Map<ResourceLocation, Recipe<?>> bucket = map.get(recipe.getType());
        if (bucket != null) bucket.remove(recipe.getId());
        byName.remove(recipe.getId());
    }

    // RecipeManager 的 recipes / byName 可能是不可变或并发映射（KubeJS post 之后），先拷贝成可变的
    private static Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> mutableRecipes(RecipeManager manager) {
        if (manager.recipes instanceof HashMap) {
            return (Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>) manager.recipes;
        }
        HashMap<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> copy = new HashMap<>(manager.recipes);
        manager.recipes = copy;
        return copy;
    }

    private static Map<ResourceLocation, Recipe<?>> mutableByName(RecipeManager manager) {
        if (manager.byName instanceof HashMap) {
            return (Map<ResourceLocation, Recipe<?>>) manager.byName;
        }
        HashMap<ResourceLocation, Recipe<?>> copy = new HashMap<>(manager.byName);
        manager.byName = copy;
        return copy;
    }
}
