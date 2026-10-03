package com.leaf.techjs.kubejs.event;

import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.resources.ResourceLocation;
import com.leaf.techjs.system.Tech;
import org.jetbrains.annotations.Nullable;

/**
 * 安装 JEI 时触发的科技启用/禁用事件（仅客户端），监听时需要指明 Tech 的 ResourceLocation：
 * <pre>{@code
 * TechEvents.onTechEnableWithJEI('techjs:example', event => {
 *     event.id         // ResourceLocation
 *     event.tech       // Tech（可为 null）
 *     event.jeiRuntime // IJeiRuntime
 * })
 * }</pre>
 * jeiRuntime 声明为 Object，避免未安装 JEI 时加载 JEI 类；JS 端可正常调用其方法。
 */
public class TechJEIEventJS extends EventJS {
    public final ResourceLocation id;
    @Nullable
    public final Tech tech;
    @Nullable
    public final Object jeiRuntime;

    public TechJEIEventJS(ResourceLocation id, @Nullable Tech tech, @Nullable Object jeiRuntime) {
        this.id = id;
        this.tech = tech;
        this.jeiRuntime = jeiRuntime;
    }

    public ResourceLocation getId() {
        return id;
    }

    @Nullable
    public Tech getTech() {
        return tech;
    }

    @Nullable
    public Object getJeiRuntime() {
        return jeiRuntime;
    }

    @Override
    public String toString() {
        return "TechJEIEventJS{id=" + id + "}";
    }
}
