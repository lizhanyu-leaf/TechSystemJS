package com.leaf.techjs;

import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public enum CompatMods {
    JEI("StartEventObserverMixin");

    private final String id;
    private final String[] mixins;

    private Boolean loaded;

    private static Map<String, ModInfo> modInfoMap = null;

    CompatMods() {
        id = name().toLowerCase(Locale.ROOT);
        mixins = null;
    }

    CompatMods(String... mixins) {
        id = name().toLowerCase(Locale.ROOT);
        this.mixins = mixins;
    }

    // 在 mixin 应用阶段判断 mod 加载信息
    public static ModInfo getModInfo(String id) {
        if (modInfoMap == null) {
            modInfoMap = new HashMap<>();
            for (ModInfo mod: FMLLoader.getLoadingModList().getMods()) {
                modInfoMap.put(mod.getModId(), mod);
            }
        }

        return modInfoMap.get(id);
    }

    public boolean isLoaded() {
        if (loaded == null)
            loaded = getModInfo(id) != null;

        return loaded;
    }

    /**
     * Simple hook to run code if a mod is installed
     * @param toRun will be run only if the mod is loaded
     * @return Optional.empty() if the mod is not loaded, otherwise an Optional of the return value of the given supplier
     */
    public <T> Optional<T> runIfInstalled(Supplier<Supplier<T>> toRun) {
        if (isLoaded())
            return Optional.of(toRun.get().get());
        return Optional.empty();
    }

    public String[] getMixins() {
        return mixins;
    }

    public String getPath() {
        return id;
    }
}
