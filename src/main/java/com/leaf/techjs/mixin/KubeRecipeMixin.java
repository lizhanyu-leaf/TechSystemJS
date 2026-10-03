package com.leaf.techjs.mixin;

import com.leaf.techjs.system.TechRecipeHolder;
import dev.latvian.mods.kubejs.recipe.KubeRecipe;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

/**
 * 给 KubeRecipe 增加 tech 方法，在脚本中指定配方所属科技：
 * <pre>{@code
 * event.recipes.minecraft.crafting_shaped(...).tech('techjs:example')
 * }</pre>
 */
@Mixin(value = KubeRecipe.class, remap = false)
public abstract class KubeRecipeMixin implements TechRecipeHolder {

    @Unique
    private ResourceLocation techjs$tech;

    @Override
    public ResourceLocation techjs$getTech() {
        return techjs$tech;
    }

    @Override
    public void techjs$setTech(ResourceLocation tech) {
        this.techjs$tech = tech;
    }

    @Unique
    public KubeRecipe tech(ResourceLocation tech) {
        // 只保留 ResourceLocation 参数：KubeJS 会自动把符合格式的 String 转成 ResourceLocation，
        // 若再提供 String 重载会导致 Rhino 无法解析而报 ambiguous
        this.techjs$tech = tech;
        return (KubeRecipe) (Object) this;
    }
}
