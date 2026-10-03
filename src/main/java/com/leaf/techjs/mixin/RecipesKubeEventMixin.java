package com.leaf.techjs.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.leaf.techjs.system.TechRecipeHolder;
import com.leaf.techjs.system.TechRecipeManager;
import dev.latvian.mods.kubejs.core.RecipeManagerKJS;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import dev.latvian.mods.kubejs.recipe.RecipesKubeEvent;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * 有 tech 的 KubeRecipe 统一收集到 TechRecipeManager.techRecipes：
 * 未启用的科技从 datapackRecipeMap 中移除（vanilla apply 不会再加载它）；已启用的保持原样
 */
@Mixin(value = RecipesKubeEvent.class, remap = false)
public abstract class RecipesKubeEventMixin {

    @Inject(method = "post(Ldev/latvian/mods/kubejs/core/RecipeManagerKJS;Ljava/util/Map;)V",
            at = @At("HEAD"), remap = false)
    private void techjs$resetTechRecipes(RecipeManagerKJS manager, Map<ResourceLocation, JsonElement> datapackRecipeMap, CallbackInfo ci) {
        TechRecipeManager.reset();
    }

    @Inject(method = "post(Ldev/latvian/mods/kubejs/core/RecipeManagerKJS;Ljava/util/Map;)V",
            at = @At("TAIL"), remap = false)
    private void techjs$divertTechRecipes(RecipeManagerKJS manager, Map<ResourceLocation, JsonElement> datapackRecipeMap, CallbackInfo ci) {
        // .tech(...) 是在配方创建（并加入 addedRecipes）之后链式调用的，所以要等脚本全部跑完再统一收集。
        // 此时 post 已把 addedRecipes 的 JSON 合并进 datapackRecipeMap，按当前启用状态处理：
        //  - 未启用：从 datapackRecipeMap 中移除。reload 前由 toggle 时双端各自配合增删，
        //    reload 后配方重建时在这里按持久化状态重建，再由原版配方包强制双端同步，不容易出 bug
        //  - 已启用：不移除，随 vanilla apply 正常进入配方管理器
        RecipesKubeEvent self = (RecipesKubeEvent) (Object) this;
        for (KubeRecipe recipe : self.addedRecipes) {
            ResourceLocation tech = TechRecipeHolder.getTech(recipe);
            if (tech == null) continue;

            ResourceLocation recipeId = recipe.getOrCreateId();
            JsonObject json = recipe.json;
            if (recipeId == null || json == null) continue;

            if (TechRecipeManager.divert(tech, recipeId, json)) {
                datapackRecipeMap.remove(recipeId);
            }
        }
    }
}
