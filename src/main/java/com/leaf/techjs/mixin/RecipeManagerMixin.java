package com.leaf.techjs.mixin;

import com.google.gson.JsonElement;
import com.leaf.techjs.system.TechRecipeManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * RecipeManager.apply 结束时（此时 KubeJS 的 RecipesKubeEvent 已在 HEAD 注入并修改完配方，
 * vanilla 已用最终配方重建了 byName / byType），把所有科技的当前状态幂等地应用到配方管理器：
 * 未启用的科技配方被移除，已启用的补漏加回。
 */
@Mixin(value = RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Shadow
    @Final
    private net.minecraft.core.HolderLookup.Provider registries;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("TAIL"))
    private void techjs$applyTechStates(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        TechRecipeManager.applyAll(registries, (RecipeManager) (Object) this);
    }
}
