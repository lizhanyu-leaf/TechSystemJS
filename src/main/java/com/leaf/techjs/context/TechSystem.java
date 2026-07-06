package com.leaf.techjs.context;

import com.leaf.techjs.AllConfig;
import com.leaf.techjs.AllPackets;
import com.leaf.techjs.jei.RestartJEIPacket;
import com.leaf.techjs.kubejs.TechSystemEvents;
import com.leaf.techjs.kubejs.event.TechSystemRecipesEventJS;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class TechSystem {
    public static RestartDelay RESTART_DELAY;

    private static boolean shouldApply = false;

    /**
     * 当前是否存在未应用的科技变更（由 TechSystemStorage.setActive 标记）
     */
    public static boolean needsApply() {
        return shouldApply;
    }

    public static void init() {
        RESTART_DELAY = new RestartDelay(AllConfig.restartDelay);
    }

    /**
     * 创建科技配方JS，用于在重启时刷新 datapackRecipeMap 和 重启 TechnologyRecipesEventJS 时使用（其实也没有其他用途）
     */
    public static TechSystemRecipesEventJS createTechnologyRecipesEvent() {
        return new TechSystemRecipesEventJS();
    }

    /**
     * 设置科技系统 dirty，用于在科技配方更新时触发科技系统刷新
     */
    public static void setDirty() {
        shouldApply = true;
    }

    /**
     * 设置科技系统 dirty
     * @param value 是否 dirty
     */
    public static void setDirty(boolean value) {
        shouldApply = value;
    }

    /**
     * 刷新科技系统
     * @param server 服务器实例
     */
    public static void apply(MinecraftServer server) {
        if (shouldApply)
            RESTART_DELAY.start(server);
        setDirty(false);
    }

    /**
     * 延迟重启JEI
     */
    public static class RestartDelay {
        private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        private ScheduledFuture<?> future;
        private final long delayMillis;

        private static void task(MinecraftServer server) {
            if (TechSystemEvents.ON_TECHNOLOGY_LOAD.hasListeners())
                createTechnologyRecipesEvent().post(server.getRecipeManager());
            // 发送更新配方包
            var recipePacket = new ClientboundUpdateRecipesPacket(server.getRecipeManager().getRecipes());
            var restartPacket = new RestartJEIPacket();
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                player.connection.send(recipePacket);
                AllPackets.getChannel().send(PacketDistributor.PLAYER.with(()->player), restartPacket);
            }
        }

        /**
         * 创建一个延迟重启JEI的定时器
         * @param delayMillis 延迟时间，单位毫秒
         */
        public RestartDelay(long delayMillis) {
            this.delayMillis = delayMillis;
        }

        /**
         * 启动定时器
         * @param server 服务器实例
         */
        public synchronized void start(MinecraftServer server) {
            if (future != null && !future.isDone()) {
                future.cancel(false);
            }
            future = scheduler.schedule(() -> task(server), delayMillis, TimeUnit.MILLISECONDS);
        }
    }
}
