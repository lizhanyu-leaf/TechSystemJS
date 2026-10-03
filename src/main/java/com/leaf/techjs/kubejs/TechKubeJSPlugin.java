package com.leaf.techjs.kubejs;

import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.ScriptType;

public class TechKubeJSPlugin implements KubeJSPlugin {

    @Override
    public void registerBindings(BindingRegistry event) {
        if (event.type() == ScriptType.SERVER) {
            event.add("TechSystemJS", new TechSystemTool());
        }
    }

    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(TechSystemEvents.GROUP);
    }
}
