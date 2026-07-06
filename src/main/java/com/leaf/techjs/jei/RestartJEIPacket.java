package com.leaf.techjs.jei;

import com.leaf.techjs.foundation.SimplePacketBase;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class RestartJEIPacket extends SimplePacketBase {

    public RestartJEIPacket(FriendlyByteBuf buf) {}
    public RestartJEIPacket() {}

    @Override
    public void write(FriendlyByteBuf friendlyByteBuf) {}

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(JEIRestarter::restart);
        return true;
    }
}
