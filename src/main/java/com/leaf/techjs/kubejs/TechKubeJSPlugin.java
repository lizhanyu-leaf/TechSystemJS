package com.leaf.techjs.kubejs;

import com.leaf.techjs.AllItems;
import com.leaf.techjs.kubejs.event.TechSystemRegisterEventJS;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.client.LangEventJS;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.leaf.techjs.TechSystemJS.MOD_ID;

public class TechKubeJSPlugin extends KubeJSPlugin {
    @SuppressWarnings("removal")
    @Override
    public void initStartup() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        AllItems.register(modEventBus);
    }

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

    @Override
    public void generateLang(LangEventJS event) {
        var map = event.map;

        var techLang = TechSystemRegisterEventJS.getAllTechLang();
        var itemLang = TechSystemRegisterEventJS.getAllItemLang();

        for (var locale : TechSystemRegisterEventJS.getNeedListenLangLocale()) {
            var langEvent = new LangEventJS(locale, map);
            langEvent.addAll(MOD_ID, TechSystemRegisterEventJS.transform(techLang.get(locale)));
            langEvent.addAll(MOD_ID, TechSystemRegisterEventJS.transform(itemLang.get(locale)));
        }
    }
}
