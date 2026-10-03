package com.leaf.techjs.mixin;

import com.google.gson.JsonElement;
import com.leaf.techjs.system.TechRecipeHolder;
import com.leaf.techjs.system.TechRecipeManager;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Map;

/**
 * 有 tech 的 RecipeJS 统一收集到 TechRecipeManager.techRecipes：
 * 未启用的科技从 addedRecipes 与配方管理器中移除；已启用的保持原样
 */
@Mixin(value = RecipesEventJS.class, remap = false)
public abstract class RecipesEventJSMixin {

    @Inject(method = "post(Lnet/minecraft/world/item/crafting/RecipeManager;Ljava/util/Map;)V",
            at = @At("HEAD"), remap = false)
    private void techjs$resetTechRecipes(RecipeManager manager, Map<ResourceLocation, JsonElement> datapackRecipeMap, CallbackInfo ci) {
        TechRecipeManager.reset();
    }

    @Inject(method = "post(Lnet/minecraft/world/item/crafting/RecipeManager;Ljava/util/Map;)V",
            at = @At("TAIL"), remap = false)
    private void techjs$divertTechRecipes(RecipeManager manager, Map<ResourceLocation, JsonElement> datapackRecipeMap, CallbackInfo ci) {
        // .tech(...) 是在配方创建（并加入 addedRecipes）之后链式调用的，所以要等脚本全部跑完再统一收集。
        // 此时 post 已把 addedRecipes 合并进 RecipeManager，按当前启用状态处理：
        //  - 未启用：从 addedRecipes 与配方管理器中移除。reload 前由 toggle 时双端各自配合增删，
        //    reload 后配方重建时在这里按持久化状态重建，再由原版配方包强制双端同步，不容易出 bug
        //  - 已启用：不移除，随 post 正常进入配方管理器
        RecipesEventJS self = (RecipesEventJS) (Object) this;
        Iterator<RecipeJS> iterator = self.addedRecipes.iterator();
        while (iterator.hasNext()) {
            RecipeJS recipe = iterator.next();
            ResourceLocation tech = TechRecipeHolder.getTech(recipe);
            if (tech == null) continue;

            Recipe<?> vanilla = recipe.createRecipe();
            if (vanilla == null) {
                iterator.remove();
                continue;
            }

            if (TechRecipeManager.divert(tech, vanilla, manager)) {
                iterator.remove();
            }
        }
    }
}
