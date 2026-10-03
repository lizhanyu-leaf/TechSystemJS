package com.leaf.techjs.foundation;

import com.leaf.techjs.TechSystemJS;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 服务端 -> 客户端：科技启用状态变化（以及该科技的配方，供 JEI 增删）。
 * initial 为 true 时是登录 / reload 时的状态同步，只恢复状态，不触发 client script 事件；
 * 配方实例不随 initial 包发送（JEI 初始化时会自己从客户端配方管理器收录），
 * 但 recipeIds 始终携带，客户端在 JEI 就绪后按 id 从配方管理器取回实例用于后续隐藏
 */
public record UpdateTechPacket(ResourceLocation techId, boolean enable, boolean initial,
                               List<ResourceLocation> recipeIds, List<RecipeHolder<?>> recipes)
        implements CustomPacketPayload {

    public static final Type<UpdateTechPacket> TYPE = new Type<>(TechSystemJS.modLoc("update_tech"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateTechPacket> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, UpdateTechPacket::techId,
            ByteBufCodecs.BOOL, UpdateTechPacket::enable,
            ByteBufCodecs.BOOL, UpdateTechPacket::initial,
            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), UpdateTechPacket::recipeIds,
            RecipeHolder.STREAM_CODEC.apply(ByteBufCodecs.list()), UpdateTechPacket::recipes,
            UpdateTechPacket::new
    );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
