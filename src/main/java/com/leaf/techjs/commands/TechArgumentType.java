package com.leaf.techjs.commands;

import com.leaf.techjs.context.TechInfo;
import com.leaf.techjs.context.TechSystemStorage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class TechArgumentType implements ArgumentType<TechInfo> {

    private final boolean enableSuggestions;

    public TechArgumentType(boolean enableSuggestions) {
        this.enableSuggestions = enableSuggestions;
    }

    public TechArgumentType() {
        this(true);
    }

    public boolean isEnableSuggestions() {
        return enableSuggestions;
    }

    public static TechArgumentType tech() {
        return new TechArgumentType();
    }

    public static TechArgumentType noSuggestions() {
        return new TechArgumentType(false);
    }

    @Override
    public TechInfo parse(StringReader reader) throws CommandSyntaxException {
        ResourceLocation id = ResourceLocation.read(reader);
        return TechInfo.of(id);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(
            final CommandContext<S> context, final SuggestionsBuilder builder) {
        if (!enableSuggestions) return Suggestions.empty();
        TechSystemStorage storage = TechSystemStorage.getInstance();
        if (storage == null) return Suggestions.empty();
        for (TechInfo tech : storage.getAllTechs()) {
            builder.suggest(tech.id.toString());
        }
        return builder.buildFuture();
    }

    public static TechInfo getTech(CommandContext<?> context, String name) {
        return context.getArgument(name, TechInfo.class);
    }
}
