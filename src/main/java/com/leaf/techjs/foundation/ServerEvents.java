package com.leaf.techjs.foundation;

import com.leaf.techjs.AllPackets;
import com.leaf.techjs.TechSystemJS;
import com.leaf.techjs.kubejs.TechSystemEvents;
import com.leaf.techjs.kubejs.event.RegisterTechEventJS;
import com.leaf.techjs.system.TechManager;
import com.leaf.techjs.system.TechRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = TechSystemJS.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {
    public static @Nullable MinecraftServer server;

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        server = event.getServer();
        // 服务器脚本在 AboutToStart 时已加载完（早于 ServerStartingEvent），
        // 先从存档恢复科技状态，再触发 registerTech 事件：
        // 让 TechManager.register 把每个注册的科技以默认 false 写入存档文件
        TechManager.load(event.getServer());
        TechSystemEvents.REGISTER.post(new RegisterTechEventJS());
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        // 兜底：配方事件（divert）无论在 load() 之前还是之后运行，
        // 启动完成后把所有科技的当前状态幂等地应用到配方管理器
        TechRecipeManager.applyAll();
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
            TechSystemEvents.REGISTER.post(new RegisterTechEventJS());
        }

        List<ServerPlayer> players = event.getPlayer() != null
                ? List.of(event.getPlayer())
                : event.getPlayerList().getPlayers();

        for (ServerPlayer player : players) {
            for (Map.Entry<ResourceLocation, Boolean> entry : TechManager.getStates().entrySet()) {
                // initial 同步：客户端只恢复状态与配方 id 列表，配方已随登录配方包 / JEI 初始化到位
                UpdateTechPacket packet = new UpdateTechPacket(
                        entry.getKey(), entry.getValue(), true, TechRecipeManager.getRecipeIds(entry.getKey()), List.of());
                AllPackets.getChannel().send(PacketDistributor.PLAYER.with(() -> player), packet);
            }
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        server = null;
    }
}
