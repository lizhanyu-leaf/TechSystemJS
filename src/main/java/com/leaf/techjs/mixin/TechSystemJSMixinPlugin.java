package com.leaf.techjs.mixin;

import com.leaf.techjs.CompatMods;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TechSystemJSMixinPlugin implements IMixinConfigPlugin {
    private static final String PATH = "com.leaf.techjs.mixin";
    private static Set<String> blacklist;

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (blacklist == null) {
            blacklist = new HashSet<>();
            for (CompatMods mod: CompatMods.values()) {
                if (!mod.isLoaded() && mod.getMixins() != null) {
                    for (String mixin: mod.getMixins()) {
                        blacklist.add(String.format("%s.%s.%s", PATH, mod.getPath(), mixin));
                    }
                }
            }
        }

        return !blacklist.contains(mixinClassName);
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
