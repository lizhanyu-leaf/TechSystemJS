package com.leaf.techjs.kubejs.event;

import com.leaf.techjs.system.Tech;
import com.leaf.techjs.system.TechManager;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;

public class RegisterTechEventJS extends EventJS {

    public void registerTech(ResourceLocation location, Consumer<CompoundTag> consumer) {
        var tag = new CompoundTag();
        consumer.accept(tag);
        TechManager.register(new Tech(location, tag));
    }
}
