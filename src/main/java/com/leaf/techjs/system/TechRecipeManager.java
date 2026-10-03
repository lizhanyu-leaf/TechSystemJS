package com.leaf.techjs.system;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.leaf.techjs.TechSystemJS;
import com.leaf.techjs.foundation.ServerEvents;
import com.leaf.techjs.foundation.UpdateTechPacket;
import dev.latvian.mods.kubejs.core.RecipeManagerKJS;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 1.21.1 中 RecipeManager 的 byName / byType 均为私有不可变集合，
 * 这里借助 KubeJS 注入的 {@link RecipeManagerKJS}（kjs$getRecipeIdMap / kjs$replaceRecipes）完成增删，
 * 与 KubeJS 自身重建配方表的方式保持一致。
 */
public final class TechRecipeManager {

    /**
     * 带科技标记的配方（由 RecipesKubeEventMixin 在 RecipesKubeEvent.post 结束时收集）。
     * KubeJS 7 中配方以 JSON 形式合并进 datapack map、随 RecipeManager.apply 重建，
     * 因此这里保存 (配方 id, JSON)，需要时再解码成 {@link RecipeHolder}
     */
    public record TaggedRecipe(ResourceLocation id, JsonObject json) {
    }

    /** tech id -> 配方列表 */
    static final Map<ResourceLocation, List<TaggedRecipe>> techRecipes = new HashMap<>();

    /**
     * RecipesKubeEventMixin 在 RecipesKubeEvent.post 开始时调用：
     * 脚本即将重跑，清空旧配方，随后会按当前启用状态重新收集
     */
    public static void reset() {
        techRecipes.clear();
    }

    /**
     * RecipesKubeEventMixin 在 RecipesKubeEvent.post 结束时调用（此时 addedRecipes 已合并进 datapackRecipeMap）：
     * <ul>
     *     <li>科技未启用：从 datapackRecipeMap 中移除，使 vanilla apply 不再加载它</li>
     *     <li>科技已启用：保持原样，随 apply 正常进入配方管理器（reload 后由原版配方包强制双端同步）</li>
     * </ul>
     *
     * @return 配方是否应从 datapackRecipeMap 中移除（即科技未启用）
     */
    public static boolean divert(ResourceLocation techId, ResourceLocation recipeId, JsonObject json) {
        techRecipes.computeIfAbsent(techId, k -> new ArrayList<>()).add(new TaggedRecipe(recipeId, json));
        return !TechManager.isEnable(techId);
    }

    /**
     * 科技启停时调用（TechManager.toggle）：更新配方管理器并同步客户端
     */
    static void update(ResourceLocation id) {
        MinecraftServer server = ServerEvents.server;
        if (server == null) return;

        apply(server.registryAccess(), server.getRecipeManager(), id);

        // 同步客户端：下发配方 + 科技状态（客户端再据此更新 JEI、触发 client script 事件）
        RecipeManager manager = server.getRecipeManager();
        ClientboundUpdateRecipesPacket recipePacket = new ClientboundUpdateRecipesPacket(manager.getRecipes());
        UpdateTechPacket techPacket = new UpdateTechPacket(
                id, TechManager.isEnable(id), false, getRecipeIds(id), decode(server.registryAccess(), id));

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.connection.send(recipePacket);
            PacketDistributor.sendToPlayer(player, techPacket);
        }
    }

    /** 某个科技的配方 id 列表（客户端同步用） */
    public static List<ResourceLocation> getRecipeIds(ResourceLocation id) {
        return techRecipes.getOrDefault(id, List.of()).stream().map(TaggedRecipe::id).toList();
    }

    /**
     * 把所有科技的当前状态幂等地应用到配方管理器（不发包）。
     * 在 RecipeManager.apply 结束时（Mixin）调用；服务器启动完成时（ServerStartedEvent）再调用一次，
     * 兜底纠正配方事件（divert）先于状态加载运行的时序
     */
    public static void applyAll(HolderLookup.Provider registries, RecipeManager manager) {
        if (techRecipes.isEmpty()) return;
        for (ResourceLocation id : techRecipes.keySet()) {
            apply(registries, manager, id);
        }
    }

    /** 把某个科技当前的启用状态应用到配方管理器（不发包） */
    private static void apply(HolderLookup.Provider registries, RecipeManagerKJS kjs, ResourceLocation id) {
        List<TaggedRecipe> recipes = techRecipes.get(id);
        if (recipes == null || recipes.isEmpty()) return;

        Map<ResourceLocation, RecipeHolder<?>> byName = new LinkedHashMap<>(kjs.kjs$getRecipeIdMap());

        if (TechManager.isEnable(id)) {
            for (TaggedRecipe recipe : recipes) {
                // 正常 reload 流程中配方已随 datapack map 进入管理器，只补漏（如首次启动时状态尚未加载）
                if (byName.containsKey(recipe.id())) continue;
                RecipeHolder<?> holder = decode(registries, recipe);
                if (holder != null) {
                    byName.put(recipe.id(), holder);
                }
            }
        } else {
            recipes.forEach(recipe -> byName.remove(recipe.id()));
        }

        kjs.kjs$replaceRecipes(byName);
    }

    /** 某个科技当前的全部配方实例（发包给客户端、供 JEI 增删用） */
    private static List<RecipeHolder<?>> decode(HolderLookup.Provider registries, ResourceLocation id) {
        List<RecipeHolder<?>> holders = new ArrayList<>();
        for (TaggedRecipe recipe : techRecipes.getOrDefault(id, List.of())) {
            RecipeHolder<?> holder = decode(registries, recipe);
            if (holder != null) holders.add(holder);
        }
        return holders;
    }

    /** 把配方 JSON 解码成 {@link RecipeHolder}（与 vanilla RecipeManager.apply 的解析方式一致） */
    private static @Nullable RecipeHolder<?> decode(HolderLookup.Provider registries, TaggedRecipe recipe) {
        try {
            RegistryOps<JsonElement> ops = registries.createSerializationContext(JsonOps.INSTANCE);
            return Recipe.CONDITIONAL_CODEC.parse(ops, recipe.json()).result()
                    .flatMap(withConditions -> withConditions.map(WithConditions::carrier))
                    .map(value -> new RecipeHolder<>(recipe.id(), value))
                    .orElse(null);
        } catch (Exception e) {
            TechSystemJS.LOGGER.error("[TechRecipeManager] : 解码配方 {} 失败: {}", recipe.id(), e.getMessage());
            return null;
        }
    }
}
