package com.leaf.techjs.system;

import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * 由 {@code KubeRecipeMixin} 注入到 {@link KubeRecipe} 的 duck interface，
 * 用于读取脚本端 {@code recipe.tech(...)} 指定的科技。
 */
public interface TechRecipeHolder {

    @Nullable
    ResourceLocation techjs$getTech();

    void techjs$setTech(@Nullable ResourceLocation tech);

    static @Nullable ResourceLocation getTech(KubeRecipe recipe) {
        return recipe instanceof TechRecipeHolder holder ? holder.techjs$getTech() : null;
    }
}
