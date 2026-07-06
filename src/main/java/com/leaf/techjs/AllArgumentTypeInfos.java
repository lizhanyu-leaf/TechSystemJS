package com.leaf.techjs;

import com.leaf.techjs.commands.TechArgumentType;
import com.leaf.techjs.commands.TechArgumentTypeInfo;
import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public enum AllArgumentTypeInfos {
    TECH("tech", TechArgumentType.class, new TechArgumentTypeInfo());

    private final String id;
    private final Class<? extends ArgumentType<?>> clazz;
    private final ArgumentTypeInfo<?, ?> instance;

    AllArgumentTypeInfos(String id, Class<? extends ArgumentType<?>> clazz, ArgumentTypeInfo<?, ?> instance) {
        this.id = id;
        this.clazz = clazz;
        this.instance = instance;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void register(IEventBus bus) {

        for (AllArgumentTypeInfos info : values()) {
            // registerByClass is generic; use unchecked casts because enum stores wildcarded types
            ArgumentTypeInfos.registerByClass((Class) info.clazz, (ArgumentTypeInfo) info.instance);
            ARGUMENT_TYPES.register(info.id, () -> info.instance);
        }

        ARGUMENT_TYPES.register(bus);
    }

    public static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_TYPES
            = DeferredRegister.create(ForgeRegistries.COMMAND_ARGUMENT_TYPES, TechSystemJS.MOD_ID);
}
