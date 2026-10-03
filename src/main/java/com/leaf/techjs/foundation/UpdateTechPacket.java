package com.leaf.techjs.foundation;

import com.leaf.techjs.system.ClientTechSync;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;

/**
 * 服务端 -> 客户端：科技启用状态变化（以及该科技的配方，供 JEI 增删）。
 * initial 为 true 时是登录 / reload 时的状态同步，只恢复状态，不触发 client script 事件；
 * 配方实例不随 initial 包发送（JEI 初始化时会自己从客户端配方管理器收录），
 * 但 recipeIds 始终携带，客户端在 JEI 就绪后按 id 从配方管理器取回实例用于后续隐藏
 */
public class UpdateTechPacket extends SimplePacketBase {

    public final ResourceLocation techId;
    public final boolean enable;
    public final boolean initial;
    public final List<ResourceLocation> recipeIds;
    public final List<Recipe<?>> recipes;

    public UpdateTechPacket(ResourceLocation techId, boolean enable, boolean initial,
                            List<ResourceLocation> recipeIds, List<Recipe<?>> recipes) {
        this.techId = techId;
        this.enable = enable;
        this.initial = initial;
        this.recipeIds = recipeIds;
        this.recipes = recipes;
    }

    public UpdateTechPacket(FriendlyByteBuf buf) {
        this.techId = buf.readResourceLocation();
        this.enable = buf.readBoolean();
        this.initial = buf.readBoolean();
        this.recipeIds = buf.readList(FriendlyByteBuf::readResourceLocation);
        this.recipes = buf.readList(ClientboundUpdateRecipesPacket::fromNetwork);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeResourceLocation(techId);
        buf.writeBoolean(enable);
        buf.writeBoolean(initial);
        buf.writeCollection(recipeIds, FriendlyByteBuf::writeResourceLocation);
        buf.writeCollection(recipes, ClientboundUpdateRecipesPacket::toNetwork);
    }

    @Override
    public boolean handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> ClientTechSync.handleTech(this));
        return true;
    }
}
