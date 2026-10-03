package com.leaf.techjs.foundation;

import com.leaf.techjs.TechSystemJS;
import com.leaf.techjs.kubejs.TechSystemEvents;
import com.leaf.techjs.kubejs.event.RegisterTechEventJS;
import com.leaf.techjs.system.TechManager;
import com.leaf.techjs.system.TechRecipeManager;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = TechSystemJS.MOD_ID)
public class ServerEvents {
    public static @Nullable MinecraftServer server;

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        server = event.getServer();
        // 服务器脚本在数据包加载阶段已执行完（早于 ServerStartingEvent），
        // 先从存档恢复科技状态，再触发 registerTech 事件：
        // 让 TechManager.register 把每个注册的科技以默认 false 写入存档文件
        TechManager.load(event.getServer());
        TechSystemEvents.REGISTER.post(ScriptType.SERVER, new RegisterTechEventJS());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        // 兜底：配方事件（divert）运行在数据包加载阶段，早于状态加载，
        // 启动完成后把所有科技的当前状态幂等地应用到配方管理器
        TechRecipeManager.applyAll(event.getServer().registryAccess(), event.getServer().getRecipeManager());
    }

    /**
     * /reload 时在重载管线执行之前触发：重新读取存档里的科技状态，
     * 这样手动编辑 tech_system.json 后 /reload 即可生效
     */
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        if (server != null) {
            TechManager.load(server);
        }
    }

    /**
     * 玩家登录（player != null）或 /reload（player == null）时触发，且早于原版下发配方包：
     * reload 完成后（脚本已重载）重新触发 registerTech 补齐新注册的科技，再向玩家同步科技状态
     */
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            TechSystemEvents.REGISTER.post(ScriptType.SERVER, new RegisterTechEventJS());
        }

        List<ServerPlayer> players = event.getPlayer() != null
                ? List.of(event.getPlayer())
                : event.getPlayerList().getPlayers();

        for (ServerPlayer player : players) {
            for (Map.Entry<ResourceLocation, Boolean> entry : TechManager.getStates().entrySet()) {
                // initial 同步：客户端只恢复状态与配方 id 列表，配方已随登录配方包 / JEI 初始化到位
                UpdateTechPacket packet = new UpdateTechPacket(
                        entry.getKey(), entry.getValue(), true, TechRecipeManager.getRecipeIds(entry.getKey()), List.of());
                PacketDistributor.sendToPlayer(player, packet);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        server = null;
    }
}
