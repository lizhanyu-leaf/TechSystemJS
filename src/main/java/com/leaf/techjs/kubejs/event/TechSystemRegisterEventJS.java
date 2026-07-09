package com.leaf.techjs.kubejs.event;

import com.google.common.base.Stopwatch;
import com.leaf.techjs.TechSystemJS;
import com.leaf.techjs.context.TechInfo;
import com.leaf.techjs.context.TechSystemStorage;
import com.leaf.techjs.context.items.TechItemInfo;
import com.leaf.techjs.context.items.TechItemsConfig;
import com.leaf.techjs.kubejs.TechSystemEvents;
import dev.latvian.mods.kubejs.event.StartupEventJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class TechSystemRegisterEventJS extends StartupEventJS {
    private static final List<TechInfoBuilder> builders = new ArrayList<>();
    private static final Set<String> needListenLangLocale = new HashSet<>();

    private static final Map<String, List<Pair<String, String>>> allItemLang = new HashMap<>();
    private static final Map<String, List<Pair<String, String>>> allTechLang = new HashMap<>();

    public TechInfoBuilder create(String id) {
        var builder = new TechInfoBuilder(id);
        builders.add(builder);
        return builder;
    }

    private static boolean postAttempted = false;

    public static void post() {
        // 防止重复调用
        if (postAttempted) {
            TechSystemJS.LOGGER.info("TechSystemRegisterEventJS.post() already attempted, skipping.");
            return;
        }

        // 调试信息：检查 KubeJS 是否已加载
        var hasListeners = TechSystemEvents.REGISTER_TECH.hasListeners();
        TechSystemJS.LOGGER.info("TechSystemRegisterEventJS.post() - Has listeners: " + hasListeners);

        if (!hasListeners) {
            TechSystemJS.LOGGER.warn("No listeners for REGISTER_TECH event. KubeJS might not be fully initialized yet.");
            // 不直接返回，而是标记为已尝试，允许外部重试
            postAttempted = true;
            return;
        }

        postAttempted = true;

        ConsoleJS.STARTUP.info("Registering tech items...");
        var timer = Stopwatch.createStarted();
        TechSystemEvents.REGISTER_TECH
                .post(ScriptType.STARTUP, new TechSystemRegisterEventJS());

        Map<String, Map<String, String>> data = new HashMap<>();
        List<TechItemInfo> items = new ArrayList<>();

        for (var builder : builders) {
            var d = builder.buildData();
            data.put(d.getLeft(), d.getRight());
            items.add(builder.build());

            for (var locale : needListenLangLocale) {
                if (!allTechLang.containsKey(locale))
                    allTechLang.put(locale, new ArrayList<>());
                if (!allItemLang.containsKey(locale))
                    allItemLang.put(locale, new ArrayList<>());

                if (builder.lang.containsKey(locale))
                    allTechLang.get(locale).add(Pair.of(
                            "techjs.tech." + builder.id.getPath(), builder.lang.get(locale)));
                if (builder.itemLang.containsKey(locale))
                    allItemLang.get(locale).add(Pair.of(
                            "item.techjs." + builder.createItemId().itemId, builder.itemLang.get(locale)));
            }
        }

        TechSystemStorage.whenCreate(storage -> {
            for (var builder : builders) {
                var techInfo = TechInfo.of(builder.id);
                storage.addTech(techInfo);
            }
        });

        ConsoleJS.STARTUP.info("Registered " + items.size() + " tech items in " + timer.stop());

        TechItemsConfig.saveData(data);
        TechItemsConfig.setTechItems(items);
    }

    public static Set<String> getNeedListenLangLocale() {
        return needListenLangLocale;
    }

    public static Map<String, List<Pair<String, String>>> getAllItemLang() {
        return allItemLang;
    }

    public static Map<String, List<Pair<String, String>>> getAllTechLang() {
        return allTechLang;
    }

    public static Map<String, String> transform(List<Pair<String, String>> o) {
        var map = new HashMap<String, String>();
        for (var pair : o) {
            map.put(pair.getLeft(), pair.getRight());
        }
        return map;
    }

    public static class TechInfoBuilder {
        private final ResourceLocation id;
        private ResourceLocation icon;
        private String itemId;
        private final Map<String, String> lang     = new HashMap<>();
        private final Map<String, String> itemLang = new HashMap<>();

        private TechInfoBuilder(String id) {
            this.id = ResourceLocation.tryParse(id);
        }

        public TechInfoBuilder icon(String icon) {
            if (icon != null) {
                this.icon = ResourceLocation.tryParse(icon);
            }
            return this;
        }

        public TechInfoBuilder itemId(String itemId) {
            if (itemId != null) {
                this.itemId = itemId;
            }
            return this;
        }

        public TechInfoBuilder createItemId() {
            if (itemId == null) {
                this.itemId = id.getPath() + "_unlocker";
            }
            return this;
        }

        public TechInfoBuilder lang(String locale, String str) {
            needListenLangLocale.add(locale);
            this.lang.put(locale, str);
            return this;
        }

        public TechInfoBuilder itemLang(String locale, String str) {
            needListenLangLocale.add(locale);
            this.itemLang.put(locale, str);
            return this;
        }

        @HideFromJS
        public TechItemInfo build() {
            if (id == null) {
                ConsoleJS.STARTUP.warn("Tech item has no id");
                return null;
            }
            if (icon == null) {
                ConsoleJS.STARTUP.warn("Tech item " + id + " has no icon, using default icon");
                return null;
            }
            return TechItemInfo.of(
                    itemId == null ? id.getPath() + "_unlocker" : itemId,
                    id, icon
            );
        }

        @HideFromJS
        public Pair<String, Map<String, String>> buildData() {
            if (id == null) {
                ConsoleJS.STARTUP.warn("Tech item has no id");
                return null;
            }
            if (icon == null) {
                ConsoleJS.STARTUP.warn("Tech item " + id + " has no icon, using default icon");
                return null;
            }
            var data = new HashMap<String, String>();
            data.put("item_id", itemId == null ? id.getPath() + "_unlocker" : itemId);
            data.put("icon", icon.toString());
            return Pair.of(id.toString(), data);
        }
    }
}
