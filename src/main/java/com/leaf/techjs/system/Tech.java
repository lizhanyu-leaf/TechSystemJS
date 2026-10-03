package com.leaf.techjs.system;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record Tech(ResourceLocation id, CompoundTag customData) {
    @Override
    public ResourceLocation id() {
        return id;
    }

    @Override
    public CompoundTag customData() {
        return customData;
    }

    @Override
    public @NotNull String toString() {
        return "Tech{" +
                "id=" + id +
                ", customData=" + customData +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof String s) return Objects.equals(s, id.toString());
        if (o instanceof ResourceLocation location) return Objects.equals(location, id);
        if (!(o instanceof Tech tech)) return false;
        return Objects.equals(id, tech.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customData);
    }
}
