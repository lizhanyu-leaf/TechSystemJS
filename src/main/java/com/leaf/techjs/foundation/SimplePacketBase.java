package com.leaf.techjs.foundation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public abstract class SimplePacketBase {
    public SimplePacketBase(FriendlyByteBuf buf) {}
    public SimplePacketBase() {}

    public abstract void write(FriendlyByteBuf buf);
    public abstract boolean handle(NetworkEvent.Context context);
}
