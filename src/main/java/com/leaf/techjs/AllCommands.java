package com.leaf.techjs;

import com.leaf.techjs.system.Tech;
import com.leaf.techjs.system.TechManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.concurrent.CompletableFuture;

/**
 * 测试指令（需要权限等级 2）：
 * <pre>
 * /techjs toggle &lt;tech&gt; &lt;true|false&gt;   切换科技启停（更新配方、同步客户端、触发 JS 事件）
 * /techjs query &lt;tech&gt;                  查询科技状态
 * /techjs list                          列出所有科技及状态
 * </pre>
 */
@EventBusSubscriber(modid = TechSystemJS.MOD_ID)
public final class AllCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        if (!Config.ENABLE_COMMANDS.get()) return;

        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("techjs")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("toggle")
                        .then(techArgument()
                                .then(Commands.argument("enable", BoolArgumentType.bool())
                                        .executes(ctx -> {
                                            ResourceLocation id = getId(ctx, "tech");
                                            boolean enable = BoolArgumentType.getBool(ctx, "enable");
                                            TechManager.toggle(id, enable);
                                            feedback(ctx.getSource(), "Tech " + id + " -> "
                                                    + (enable ? "enabled" : "disabled"));
                                            return 1;
                                        }))))
                .then(Commands.literal("query")
                        .then(techArgument()
                                .executes(ctx -> {
                                    ResourceLocation id = getId(ctx, "tech");
                                    Tech tech = TechManager.getTech(id);
                                    feedback(ctx.getSource(), "Tech " + id + " is "
                                            + (TechManager.isEnable(id) ? "enabled" : "disabled")
                                            + (tech == null ? " (未注册)" : ""));
                                    return 1;
                                })))
                .then(Commands.literal("list")
                        .executes(ctx -> {
                            StringBuilder sb = new StringBuilder("Techs:");
                            TechManager.getAllTechIds().forEach(id -> sb.append('\n').append("  ").append(id)
                                    .append(" = ").append(TechManager.isEnable(id) ? "enabled" : "disabled"));
                            feedback(ctx.getSource(), sb.toString());
                            return 1;
                        })));
    }

    private static RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> techArgument() {
        return Commands.argument("tech", ResourceLocationArgument.id())
                .suggests(AllCommands::suggestTechs);
    }

    private static CompletableFuture<Suggestions> suggestTechs(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        TechManager.getAllTechIds().forEach(id -> builder.suggest(id.toString()));
        return builder.buildFuture();
    }

    private static ResourceLocation getId(CommandContext<CommandSourceStack> ctx, String name) {
        return ResourceLocationArgument.getId(ctx, name);
    }

    private static void feedback(CommandSourceStack source, String message) {
        if (!Config.COMMAND_OUTPUT.get()) return;
        source.sendSuccess(() -> Component.literal("[TechSystemJS] " + message), true);
    }
}
