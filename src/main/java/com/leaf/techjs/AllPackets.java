package com.leaf.techjs;

import com.leaf.techjs.foundation.UpdateTechPacket;
import com.leaf.techjs.system.ClientTechSync;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = TechSystemJS.MOD_ID)
public final class AllPackets {

    public static final int NETWORK_VERSION = 1;

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(String.valueOf(NETWORK_VERSION));

        registrar.playToClient(UpdateTechPacket.TYPE, UpdateTechPacket.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientTechSync.handleTech(payload)));
    }
}
