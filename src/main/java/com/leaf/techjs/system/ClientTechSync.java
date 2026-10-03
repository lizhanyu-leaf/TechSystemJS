package com.leaf.techjs.system;

import com.leaf.techjs.CompatMods;
import com.leaf.techjs.foundation.UpdateTechPacket;
import com.leaf.techjs.jei.TechJEI;
import com.leaf.techjs.jei.TechSystemJEI;
import com.leaf.techjs.kubejs.TechSystemEvents;
import com.leaf.techjs.kubejs.event.TechEventJS;
import com.leaf.techjs.kubejs.event.TechJEIEventJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 客户端处理 UpdateTechPacket：更新本地科技状态，
 * 安装 JEI 时增删 JEI 配方，并触发 client script 监听的 JS 事件
 *
 * <p>JEI 的 hideRecipes 按"实例"匹配（Recipe 无 equals），所以启用时把传给 JEI 的实例缓存下来，
 * 禁用时用同一批实例去隐藏；登录 / reload 后（JEI 自行从配方管理器重建了实例），
 * 在 JEI 就绪时按 id 从客户端配方管理器重新取回实例刷新缓存
 */
public final class ClientTechSync {

    /** tech -> 配方 id（initial 同步时服务端下发，用于 JEI 就绪后取回实例） */
    private static final Map<ResourceLocation, List<ResourceLocation>> recipeIds = new HashMap<>();
    /** tech -> 传给 JEI 的配方实例（启用时缓存，禁用时用于隐藏） */
    private static final Map<ResourceLocation, List<Recipe<?>>> recipeCache = new HashMap<>();

    private ClientTechSync() {
    }

    /** 仅在客户端主线程调用 */
    public static void handleTech(UpdateTechPacket packet) {
        ResourceLocation id = packet.techId;
        TechManager.setState(id, packet.enable);
        recipeIds.put(id, packet.recipeIds);

        // 登录 / reload 的 initial 同步：只恢复状态与 id 列表；
        // 配方已随登录配方包进入客户端配方管理器、JEI 初始化时会自动收录，
        // 实例缓存在 JEI 就绪时刷新（此时配方包已处理完）
        if (packet.initial) return;

        Tech tech = TechManager.getTech(id);
        if (packet.enable) {
            recipeCache.put(id, packet.recipes);
            TechSystemEvents.ON_TECH_ENABLE.post(ScriptType.CLIENT, id, new TechEventJS(id, tech));
            if (CompatMods.JEI.isLoaded()) {
                TechJEI.addRecipes(packet.recipes);
                TechSystemEvents.ON_TECH_ENABLE_WITH_JEI.post(ScriptType.CLIENT, id,
                        new TechJEIEventJS(id, tech, TechSystemJEI.jeiRuntime));
            }
        } else {
            // 用启用的同一批实例去隐藏（禁用包里的实例是重新解码的，JEI 匹配不上）
            List<Recipe<?>> cached = recipeCache.getOrDefault(id, packet.recipes);
            TechSystemEvents.ON_TECH_DISABLE.post(ScriptType.CLIENT, id, new TechEventJS(id, tech));
            if (CompatMods.JEI.isLoaded()) {
                TechJEI.removeRecipes(cached);
                TechSystemEvents.ON_TECH_DISABLE_WITH_JEI.post(ScriptType.CLIENT, id,
                        new TechJEIEventJS(id, tech, TechSystemJEI.jeiRuntime));
            }
            recipeCache.remove(id);
        }
    }

    /** JEI 运行时可用时调用：把已启用科技的配方实例缓存刷新为客户端配方管理器中的实例 */
    public static void refreshJeiRecipes() {
        RecipeManager manager = getRecipeManager();
        if (manager == null) return;

        for (Map.Entry<ResourceLocation, Boolean> entry : TechManager.getStates().entrySet()) {
            if (!entry.getValue()) continue;
            List<Recipe<?>> recipes = lookup(manager, recipeIds.get(entry.getKey()));
            if (!recipes.isEmpty()) recipeCache.put(entry.getKey(), recipes);
        }
    }

    private static List<Recipe<?>> lookup(RecipeManager manager, @Nullable List<ResourceLocation> ids) {
        List<Recipe<?>> recipes = new ArrayList<>();
        if (ids == null) return recipes;
        for (ResourceLocation id : ids) {
            manager.byKey(id).ifPresent(recipes::add);
        }
        return recipes;
    }

    private static @Nullable RecipeManager getRecipeManager() {
        var connection = Minecraft.getInstance().getConnection();
        return connection != null ? connection.getRecipeManager() : null;
    }
}
