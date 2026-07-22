package com.leaf.techjs.mixin;

import com.google.gson.JsonElement;
import com.leaf.techjs.context.TechSystemManager;
import com.leaf.techjs.context.TechSystemStorage;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
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
    private void recipesApply(Map<ResourceLocation, JsonElement> map, ResourceManager p_44038_,
                              ProfilerFiller p_44039_, CallbackInfo ci) {
        TechSystemManager.updateDatapackRecipeMapCache(map);
        if (!TechSystemStorage.hasInstance()) {
            // 避免 RecipesEventJS 在 TechSystemStorage.Instance 未创建时触发
            // 所以在 TechSystemStorage.Instance 创建后再触发一次
            TechSystemStorage.whenCreate(() -> {
                RecipesEventJS.instance = new RecipesEventJS();
                TechSystemManager.post((RecipeManager) (Object) this);
                RecipesEventJS.instance = null;
            });
        }
    }
}
