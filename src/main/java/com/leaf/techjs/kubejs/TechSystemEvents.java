package com.leaf.techjs.kubejs;

import com.leaf.techjs.kubejs.event.RegisterTechEventJS;
import com.leaf.techjs.kubejs.event.TechEventJS;
import com.leaf.techjs.kubejs.event.TechJEIEventJS;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.event.EventTargetType;
import dev.latvian.mods.kubejs.event.TargetedEventHandler;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.script.ScriptTypePredicate;
import net.minecraft.resources.ResourceLocation;

public interface TechSystemEvents {
    EventGroup GROUP = EventGroup.of("TechEvents");

    /**
     * onTechEnable / onTechDisable 在 server script 与 client script 中均可监听
     * （server 端事件发给 server script，client 端事件发给 client script），但 startup script 不行
     */
    ScriptTypePredicate SERVER_OR_CLIENT = type -> type == ScriptType.SERVER || type == ScriptType.CLIENT;

    /** 监听时必须指明 Tech 的 ResourceLocation：TechEvents.onTechEnable('techjs:xxx', event => {...}) */
    EventTargetType<ResourceLocation> REQUIRES_TECH = EventTargetType.ID;

    TargetedEventHandler<ResourceLocation> ON_TECH_ENABLE
            = GROUP.add("onTechEnable", SERVER_OR_CLIENT, () -> TechEventJS.class).requiredTarget(REQUIRES_TECH);

    TargetedEventHandler<ResourceLocation> ON_TECH_DISABLE
            = GROUP.add("onTechDisable", SERVER_OR_CLIENT, () -> TechEventJS.class).requiredTarget(REQUIRES_TECH);

    /** 安装 JEI 时在客户端额外触发，event 提供 jeiRuntime */
    TargetedEventHandler<ResourceLocation> ON_TECH_ENABLE_WITH_JEI
            = GROUP.add("onTechEnableWithJEI", SERVER_OR_CLIENT, () -> TechJEIEventJS.class).requiredTarget(REQUIRES_TECH);

    TargetedEventHandler<ResourceLocation> ON_TECH_DISABLE_WITH_JEI
            = GROUP.add("onTechDisableWithJEI", SERVER_OR_CLIENT, () -> TechJEIEventJS.class).requiredTarget(REQUIRES_TECH);

    EventHandler REGISTER = GROUP.server("registerTech", () -> RegisterTechEventJS.class);
}
