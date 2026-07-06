package com.leaf.techjs.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;

public class TechKubeJSPlugin extends KubeJSPlugin {

    @Override
    public void registerBindings(BindingsEvent event) {
        if (event.getType() == ScriptType.SERVER) {
            event.add("TechSystemJS", new TechSystemTool());
        }
    }

    @Override
    public void registerEvents() {
        TechSystemEvents.GROUP.register();
    }
}
