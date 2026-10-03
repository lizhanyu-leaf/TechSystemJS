package com.leaf.techjs.kubejs.event;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;
import com.leaf.techjs.system.Tech;
import org.jetbrains.annotations.Nullable;

/**
 * 科技启用/禁用事件，监听时需要指明 Tech 的 ResourceLocation：
 * <pre>{@code
 * TechEvents.onTechEnable('techjs:example', event => {
 *     event.id    // ResourceLocation
 *     event.tech  // Tech（可为 null，customData 在 event.tech.customData）
 * })
 * }</pre>
 * server script 与 client script 均可监听，分别在各自一侧触发。
 */
public class TechEventJS extends EventJS {
    public final ResourceLocation id;
    @Nullable
    public final Tech tech;

    public TechEventJS(ResourceLocation id, @Nullable Tech tech) {
        this.id = id;
        this.tech = tech;
    }

    public ResourceLocation getId() {
        return id;
    }

    @Nullable
    public Tech getTech() {
        return tech;
    }

    @Override
    public String toString() {
        return "TechEventJS{id=" + id + "}";
    }
}
