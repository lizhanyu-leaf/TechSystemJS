package com.leaf.techjs;

import com.leaf.techjs.context.TechInfo;
import com.leaf.techjs.context.items.TechUnlockerItem;
import com.leaf.techjs.context.items.TechItemInfo;
import com.leaf.techjs.context.items.TechItemsConfig;
import com.leaf.techjs.kubejs.event.TechSystemRegisterEventJS;
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
    private static final DeferredRegister<Item> ITEMS
            = DeferredRegister.create(ForgeRegistries.ITEMS, TechSystemJS.MOD_ID);

    private static boolean registered = false;
    public static final Map<TechInfo, RegistryObject<Item>> TECH_ITEMS = new HashMap<>();
    public static final String TECH_ITEM_ICON_ID = "tech_item_icon";
    public static final RegistryObject<Item> TECH_ITEM_ICON = ITEMS.register(TECH_ITEM_ICON_ID, () -> new Item(new Item.Properties()));

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
