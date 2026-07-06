package com.leaf.techjs.commands;

import com.google.gson.JsonObject;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

/**
 * ArgumentTypeInfo for TechArgumentType. No state is serialized (the argument type has no configurable fields),
 * so network/json serialize methods are no-ops, and the template simply instantiates a new TechArgumentType.
 */
public class TechArgumentTypeInfo implements ArgumentTypeInfo<TechArgumentType, TechArgumentTypeInfo.Template> {

    public static final TechArgumentTypeInfo INSTANCE = new TechArgumentTypeInfo();

    @Override
    public void serializeToNetwork(Template template, FriendlyByteBuf buf) {
        buf.writeBoolean(template.enableSuggestions);
    }

    @Override
    public Template deserializeFromNetwork(FriendlyByteBuf buf) {
        boolean enable = buf.readBoolean();
        return new Template(enable);
    }

    @Override
    public void serializeToJson(Template template, JsonObject json) {
        json.addProperty("enable_suggestions", template.enableSuggestions);
    }

    @Override
    public Template unpack(TechArgumentType argument) {
        return new Template(argument.isEnableSuggestions());
    }

    public static class Template implements ArgumentTypeInfo.Template<TechArgumentType> {
        private final boolean enableSuggestions;

        public Template() {
            this(true);
        }

        public Template(boolean enableSuggestions) {
            this.enableSuggestions = enableSuggestions;
        }

        @Override
        public TechArgumentType instantiate(CommandBuildContext context) {
            return new TechArgumentType(enableSuggestions);
        }

        @Override
        public ArgumentTypeInfo<TechArgumentType, ?> type() {
            return TechArgumentTypeInfo.INSTANCE;
        }
    }
}
