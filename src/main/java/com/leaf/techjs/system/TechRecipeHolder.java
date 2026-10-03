package com.leaf.techjs.system;

import net.minecraft.resources.ResourceLocation;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import org.jetbrains.annotations.Nullable;

/**
 * 由 {@code RecipeJSMixin} 注入到 {@link RecipeJS} 的 duck interface，
 * 用于读取脚本端 {@code recipe.tech(...)} 指定的科技。
 */
public interface TechRecipeHolder {

    @Nullable
    ResourceLocation techjs$getTech();

    void techjs$setTech(@Nullable ResourceLocation tech);

    static @Nullable ResourceLocation getTech(RecipeJS recipe) {
        return recipe instanceof TechRecipeHolder holder ? holder.techjs$getTech() : null;
    }
}
