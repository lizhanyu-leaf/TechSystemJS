package com.leaf.techjs.mixin;

import com.google.gson.JsonElement;
import com.leaf.techjs.context.TechSystem;
import com.leaf.techjs.context.TechSystemStorage;
import com.leaf.techjs.kubejs.event.TechSystemRecipesEventJS;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = RecipesEventJS.class, remap = false)
public class RecipesEventJSMixin {
    @Inject(method = "post", at = @At("HEAD"), remap = false)
    private void init(RecipeManager recipeManager, Map<ResourceLocation, JsonElement> datapackRecipeMap, CallbackInfo ci) {
        TechSystemRecipesEventJS event = TechSystem.createTechnologyRecipesEvent();
        event.setDatapackRecipeMap(datapackRecipeMap);
        TechSystemStorage.whenCreate(() ->
                event.post(recipeManager));
    }
}