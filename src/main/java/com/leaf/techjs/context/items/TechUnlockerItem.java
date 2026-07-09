package com.leaf.techjs.context.items;

import com.leaf.techjs.context.TechInfo;
import com.leaf.techjs.context.TechSystem;
import com.leaf.techjs.context.TechSystemStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class TechUnlockerItem extends Item {
    private final TechInfo techId;
    private final TechItemInfo info;
    private Supplier<ItemStack> itemSupplier;

    public TechUnlockerItem(TechItemInfo info) {
        super(new Properties().stacksTo(1));
        this.techId = TechInfo.of(info.getTechId());
        this.info = info;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {

        TechSystemStorage storage = TechSystemStorage.getInstance();
        if (storage == null) return InteractionResultHolder.pass(player.getItemInHand(hand));

        if (level.isClientSide()) return InteractionResultHolder.pass(player.getItemInHand(hand));

        MinecraftServer server = ((ServerLevel) level).getServer();

        if (!storage.isActive(techId)) {
            storage.setActive(techId, true);

            Component message = Component.translatable(
                    "techjs.command.unlocked",
                    Component.translatable("techjs.tech." + techId.id.getPath())
            ).withStyle(ChatFormatting.GREEN);

            server.getPlayerList().broadcastSystemMessage(message, false);

            server.execute(() -> TechSystem.apply(server));

            player.setItemInHand(hand, ItemStack.EMPTY);

            return InteractionResultHolder.success(player.getItemInHand(hand));
        } else {
            player.displayClientMessage(
                    Component.translatable(
                            "techjs.command.already_unlocked",
                            Component.translatable("techjs.tech." + techId.id.getPath())
                    ).withStyle(ChatFormatting.YELLOW), true);
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
    }

    public ItemStack getIcon() {
        if (itemSupplier == null) itemSupplier = info.getIcon();
        return itemSupplier.get();
    }
}
