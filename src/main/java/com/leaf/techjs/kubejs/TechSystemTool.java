package com.leaf.techjs.kubejs;

import com.leaf.techjs.system.Tech;
import com.leaf.techjs.system.TechManager;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

/**
 * server script 绑定：TechSystemJS.toggle('techjs:example', true)
 */
public class TechSystemTool {

    // 只提供 ResourceLocation 参数：KubeJS 会自动把符合格式的 String 转成 ResourceLocation，
    // 若再提供 String 重载会导致 Rhino 无法解析而报 ambiguous

    public void toggle(ResourceLocation id, boolean enable) {
        TechManager.toggle(id, enable);
    }

    public boolean isEnable(ResourceLocation id) {
        return TechManager.isEnable(id);
    }

    public Collection<Tech> getAllTechs() {
        return TechManager.getAllTechs();
    }
}
