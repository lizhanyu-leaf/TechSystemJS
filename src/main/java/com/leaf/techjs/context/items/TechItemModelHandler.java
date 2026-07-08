package com.leaf.techjs.context.items;

import com.leaf.techjs.AllItems;
import com.leaf.techjs.TechSystemJS;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TechSystemJS.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TechItemModelHandler {
    private static final ModelResourceLocation DEFAULT_MODEL
            = new ModelResourceLocation(TechSystemJS.MOD_ID, AllItems.TECH_ITEM_ICON_ID, "inventory");

    @SubscribeEvent
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        var models = event.getModels();
        for (var entry : TechItemsConfig.items) {
            BakedModel model;

            model = models.get(DEFAULT_MODEL);

            if (model == null) continue;
            models.put(new ModelResourceLocation(entry.getItemId(), "inventory"), model);
        }
    }
}
