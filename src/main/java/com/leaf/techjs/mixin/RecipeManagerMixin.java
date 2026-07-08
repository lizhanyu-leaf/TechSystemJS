package com.leaf.techjs.mixin;

import com.google.gson.JsonElement;
import com.leaf.techjs.TechSystemJS;
import com.leaf.techjs.context.TechSystem;
import com.leaf.techjs.context.TechSystemStorage;
import com.leaf.techjs.kubejs.TechSystemEvents;
import com.leaf.techjs.kubejs.event.TechSystemRecipesEventJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = RecipeManager.class)
public class RecipeManagerMixin {
    @Inject(method = "apply*", at = @At("HEAD"))
    private void customRecipesHead(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager,
                                   ProfilerFiller profiler, CallbackInfo ci) {
        if (TechSystemEvents.ON_TECH_LOAD.hasListeners()) {
            TechSystemJS.LOGGER.info("TechSystemJS : TechSystem Loaded");
            TechSystemRecipesEventJS event = TechSystem.createTechnologyRecipesEvent();
            event.setDatapackRecipeMap(map);
            TechSystemStorage.whenCreate(() ->
                    event.post((RecipeManager) (Object) this));
        }
    }
}
