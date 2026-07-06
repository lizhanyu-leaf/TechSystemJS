package com.leaf.techjs.context;

import net.minecraft.resources.ResourceLocation;

public class TechInfo {
    public static final TechInfo EMPTY = of("techjs:empty");

    public final ResourceLocation id;

    private TechInfo(ResourceLocation id) {
        this.id = id;
    }

    public static TechInfo of(ResourceLocation id) {
        return new TechInfo(id);
    }

    public static TechInfo of(String id) {
        return new TechInfo(ResourceLocation.parse(id));
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TechInfo techInfo = (TechInfo) obj;
        return id.equals(techInfo.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
