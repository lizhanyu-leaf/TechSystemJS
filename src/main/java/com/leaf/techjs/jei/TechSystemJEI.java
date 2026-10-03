package com.leaf.techjs.jei;

import com.leaf.techjs.TechSystemJS;
import com.leaf.techjs.system.ClientTechSync;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

@JeiPlugin
public class TechSystemJEI implements IModPlugin {

    public static IJeiRuntime jeiRuntime;

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        TechSystemJEI.jeiRuntime = jeiRuntime;
        // JEI 从客户端配方管理器重建了配方实例，刷新缓存的实例以便后续 hideRecipes 能匹配
        ClientTechSync.refreshJeiRecipes();
    }

    @Override
    public void onRuntimeUnavailable() {
        jeiRuntime = null;
    }

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return TechSystemJS.modLoc("jei_plugin");
    }
}
