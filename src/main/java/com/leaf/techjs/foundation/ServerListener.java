package com.leaf.techjs.foundation;

import com.leaf.techjs.TechSystemJS;
import com.leaf.techjs.context.TechSystemStorage;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TechSystemJS.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerListener {

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        TechSystemStorage storage = TechSystemStorage.getOrCreateInstance(event.getServer());
        TechSystemJS.LOGGER.info("科技数据已加载, 存档路径: {}", storage.getFilePath());
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        // 服务器关闭时保存并重置
        TechSystemStorage storage = TechSystemStorage.getInstance();
        if (storage == null) return;
        storage.save();
        TechSystemStorage.reset();
    }
}
