package com.leaf.techjs;

import com.leaf.techjs.context.TechInfo;
import com.leaf.techjs.context.items.TechUnlockerItem;
import com.leaf.techjs.context.items.TechItemInfo;
import com.leaf.techjs.context.items.TechItemsConfig;
import com.leaf.techjs.kubejs.event.TechSystemRegisterEventJS;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.RegisterItemDecorationsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = TechSystemJS.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AllItems {
    public static final Map<TechInfo, RegistryObject<Item>> TECH_ITEMS = new HashMap<>();

    private static final DeferredRegister<Item> ITEMS
            = DeferredRegister.create(ForgeRegistries.ITEMS, TechSystemJS.MOD_ID);

    public static final String TECH_ITEM_ICON_ID = "tech_item_icon";
    public static final RegistryObject<Item> TECH_ITEM_ICON = ITEMS.register(TECH_ITEM_ICON_ID, () -> new Item(new Item.Properties()));

    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS
            = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TechSystemJS.MOD_ID);

    public static final RegistryObject<CreativeModeTab> TECH_TAB = CREATIVE_MODE_TABS.register("tech_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.techjs.tech_tab"))
            .icon(() -> TECH_ITEM_ICON.get().getDefaultInstance())
            .displayItems((params, output) -> TECH_ITEMS.values().forEach(item -> output.accept(item.get())))
            .build());

    private static boolean registered = false;

    public static void register(IEventBus bus) {
        if (registered) return;
        registered = true;

        TechItemsConfig.load();
        TechSystemRegisterEventJS.post();
        TechItemsConfig.reset();

        for (TechItemInfo itemInfo : TechItemsConfig.items) {
            TECH_ITEMS.put(TechInfo.of(itemInfo.getTechId()), ITEMS.register(itemInfo.getItemIdToString(), () -> new TechUnlockerItem(itemInfo)));
        }

        ITEMS.register(bus);
        CREATIVE_MODE_TABS.register(bus);
    }

    @SubscribeEvent
    public static void afterRegister(FMLCommonSetupEvent event) {
        IForgeRegistry<Item> registry = ForgeRegistries.ITEMS;
        for (TechItemInfo itemInfo : TechItemsConfig.items) {
            itemInfo.getIconFromRegistry(registry);
        }
    }

    @SubscribeEvent
    public static void registerItemDecorations(RegisterItemDecorationsEvent event) {
        // tech item
        for (var object : TECH_ITEMS.values()) {
            event.register(object.get(), (graphics, font, stack, x, y) -> {
                Item item = stack.getItem();
                if (item instanceof TechUnlockerItem techUnlockerItem) {
                    if (techUnlockerItem.getIcon() == null) return false;
                    var renderX = x + 6;
                    var renderY = y + 6;

                    graphics.pose().pushPose();
                    graphics.pose().translate(renderX, renderY, 100);
                    graphics.pose().scale(0.65F, 0.65F, 0.65F);
                    graphics.renderItem(techUnlockerItem.getIcon(), 0, 0);
                    graphics.pose().popPose();
                }
                return true;
            });
        }
    }
}
