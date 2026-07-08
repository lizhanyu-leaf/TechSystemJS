package com.leaf.techjs;

import com.leaf.techjs.commands.TechArgumentType;
import com.leaf.techjs.context.TechInfo;
import com.leaf.techjs.context.TechSystem;
import com.leaf.techjs.context.TechSystemStorage;
import com.leaf.techjs.kubejs.TechSystemEvents;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = TechSystemJS.MOD_ID)
public final class AllCommands {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        if (!AllConfig.enableCommands) return;

        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(
                Commands.literal("techjs")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("reload_storage")
                                .executes(ctx -> {
                                                            CommandSourceStack src = ctx.getSource();
                                                            TechSystemStorage storage = TechSystemStorage.getInstance();
                                                            if (storage == null) {
                                                                                                                            src.sendFailure(Component.translatable("techjs.command.reload_storage.not_ready").withStyle(ChatFormatting.RED));
                                                                return 0;
                                                                                                                        }
                                                            storage.load();
                                                            if (AllConfig.enableCommandsTips) {
                                                                                                                            src.sendSuccess(() -> Component.translatable("techjs.command.reload_storage.success").withStyle(ChatFormatting.GREEN), true);
                                                                                                                            src.sendSuccess(() -> Component.translatable("techjs.command.reload_storage.hint_apply").withStyle(ChatFormatting.GRAY), false);
                                                            }
                                                            return 1;
                                                        }))
                        .then(Commands.literal("unlock")
                                .then(Commands.argument("tech_id", TechArgumentType.tech())
                                        .executes(ctx -> {
                                            TechInfo tech = TechArgumentType.getTech(ctx, "tech_id");
                                            CommandSourceStack src = ctx.getSource();
                                            TechSystemStorage storage = TechSystemStorage.getInstance();
                                            if (tech == null) {
                                                                                            src.sendFailure(Component.translatable("techjs.command.tech_not_found").withStyle(ChatFormatting.RED));
                                                return 0;
                                            }
                                            if (storage == null) {
                                                                                            src.sendFailure(Component.translatable("techjs.command.reload_storage.not_ready").withStyle(ChatFormatting.RED));
                                                return 0;
                                            }
                                            if (storage.isActive(tech)) {
                                                                                            src.sendSuccess(() -> Component.translatable("techjs.command.already_unlocked", tech.id.toString()).withStyle(ChatFormatting.YELLOW), true);
                                                return 0;
                                            }
                                            storage.setActive(tech, true);
                                            if (AllConfig.enableCommandsTips) {
                                                                                            src.sendSuccess(() -> Component.translatable("techjs.command.unlocked", tech.id.toString()).withStyle(ChatFormatting.GREEN), true);
                                            }
                                            return 1;
                                        })))
                        .then(Commands.literal("lock")
                                .then(Commands.argument("tech_id", TechArgumentType.tech())
                                        .executes(ctx -> {
                                            TechInfo tech = TechArgumentType.getTech(ctx, "tech_id");
                                            CommandSourceStack src = ctx.getSource();
                                            TechSystemStorage storage = TechSystemStorage.getInstance();
                                            if (tech == null) {
                                                src.sendFailure(Component.translatable("techjs.command.tech_not_found").withStyle(ChatFormatting.RED));
                                                return 0;
                                            }
                                            if (storage == null) {
                                                src.sendFailure(Component.translatable("techjs.command.reload_storage.not_ready").withStyle(ChatFormatting.RED));
                                                return 0;
                                            }
                                                                    boolean exists = storage.getAll().containsKey(tech);
                                                                    if (!exists) {
                                                                        // 不存在则添加为未激活
                                                                        storage.setActive(tech, false);
                                                                        if (AllConfig.enableCommandsTips) {
                                                                            src.sendSuccess(() -> Component.translatable("techjs.command.lock_added_hint", tech.id.toString()).withStyle(ChatFormatting.GREEN), true);
                                                                            src.sendSuccess(() -> Component.translatable("techjs.command.reload_storage.hint_apply").withStyle(ChatFormatting.GRAY), false);
                                                                        }
                                                                        return 1;
                                                                    }
                                                                    if (!storage.isActive(tech)) {
                                                                        src.sendSuccess(() -> Component.translatable("techjs.command.already_locked", tech.id.toString()).withStyle(ChatFormatting.YELLOW), true);
                                                                        return 0;
                                                                    }
                                                                    storage.setActive(tech, false);
                                                                    if (AllConfig.enableCommandsTips) {
                                                                        src.sendSuccess(() -> Component.translatable("techjs.command.locked", tech.id.toString()).withStyle(ChatFormatting.GREEN), true);
                                                                    }
                                                                    return 1;
                                                                })))
                        .then(Commands.literal("apply")
                                .executes(ctx -> {
                                                            CommandSourceStack src = ctx.getSource();
                                                            try {
                                                                if (!TechSystem.needsApply()) {
                                                                    if (AllConfig.enableCommandsTips) {
                                                                                                                                        src.sendSuccess(() -> Component.translatable("techjs.command.apply.no_changes").withStyle(ChatFormatting.YELLOW), true);
                                                                    }
                                                                    return 0;
                                                                }
                                                                if (!TechSystemEvents.ON_TECH_LOAD.hasListeners()) {
                                                                    if (AllConfig.enableCommandsTips) {
                                                                        src.sendSuccess(() -> Component.translatable("techjs.command.apply.no_listener").withStyle(ChatFormatting.YELLOW), true);
                                                                    }
                                                                    return 0;
                                                                }
                                                                TechSystem.apply(src.getServer());
                                                                if (AllConfig.enableCommandsTips) {
                                                                                                                                    src.sendSuccess(() -> Component.translatable("techjs.command.apply.success").withStyle(ChatFormatting.GREEN), true);
                                                                }
                                                                return 1;
                                                            } catch (Exception e) {
                                                                src.sendFailure(Component.literal("应用失败: " + e.getMessage()).withStyle(ChatFormatting.RED));
                                                                return 0;
                                                            }
                                                        }))
                        .then(Commands.literal("list")
                                .executes(ctx -> {
                                                            CommandSourceStack src = ctx.getSource();
                                                            TechSystemStorage storage = TechSystemStorage.getInstance();
                                                            if (storage == null) {
                                                                                                                            src.sendFailure(Component.translatable("techjs.command.reload_storage.not_ready").withStyle(ChatFormatting.RED));
                                                                return 0;
                                                            }
                                                            var techs = storage.getAllActive();
                                                            if (techs.isEmpty()) {
                                                                                                                            if (AllConfig.enableCommandsTips) src.sendSuccess(() -> Component.translatable("techjs.command.list.none").withStyle(ChatFormatting.YELLOW), false);
                                                                return 1;
                                                            }
                                                            if (AllConfig.enableCommandsTips) {
                                                                                                                            src.sendSuccess(() -> Component.translatable("techjs.command.list.header", techs.size()).withStyle(ChatFormatting.AQUA), false);
                                                                for (TechInfo tech : techs) {
                                                                                                                                src.sendSuccess(() -> Component.translatable("techjs.command.list.item", tech.id.toString()).withStyle(ChatFormatting.GREEN), false);
                                                                }
                                                                // 额外提示：未被操作过的科技不会出现在列表中
                                                                                                                            src.sendSuccess(() -> Component.translatable("techjs.command.list.note").withStyle(ChatFormatting.GRAY), false);
                                                            }
                                                            return 1;
                                                        }))
                        .then(Commands.literal("add_tech")
                                .then(Commands.argument("tech_id", TechArgumentType.noSuggestions())
                                        .executes(ctx -> {
                                            CommandSourceStack src = ctx.getSource();
                                            TechInfo tech = TechArgumentType.getTech(ctx, "tech_id");
                                            TechSystemStorage storage = TechSystemStorage.getInstance();
                                            if (tech == null) {
                                                                                            src.sendFailure(Component.translatable("techjs.command.tech_not_found").withStyle(ChatFormatting.RED));
                                                return 0;
                                            }
                                            if (storage == null) {
                                                                                            src.sendFailure(Component.translatable("techjs.command.reload_storage.not_ready").withStyle(ChatFormatting.RED));
                                                return 0;
                                            }
                                            boolean exists = storage.getAll().containsKey(tech);
                                            if (exists) {
                                                                                            src.sendSuccess(() -> Component.translatable("techjs.command.exists", tech.id.toString()).withStyle(ChatFormatting.YELLOW), true);
                                                return 0;
                                            }
                                            storage.setActive(tech, false);
                                            storage.save();
                                            if (AllConfig.enableCommandsTips) {
                                                                                            src.sendSuccess(() -> Component.translatable("techjs.command.added", tech.id.toString()).withStyle(ChatFormatting.GREEN), true);
                                                                                            src.sendSuccess(() -> Component.translatable("techjs.command.reload_storage.hint_apply").withStyle(ChatFormatting.GRAY), false);
                                            }
                                            return 1;
                                        })))
                        .then(Commands.literal("help")
                                .executes(ctx -> {
                                    CommandSourceStack src = ctx.getSource();
                                    if (AllConfig.enableCommandsTips) {
                                                                            src.sendSuccess(() -> Component.translatable("techjs.command.help.title").withStyle(ChatFormatting.GOLD), false);
                                                                            src.sendSuccess(() -> Component.translatable("techjs.command.help.list").withStyle(ChatFormatting.AQUA), false);
                                                                            src.sendSuccess(() -> Component.translatable("techjs.command.help.lock").withStyle(ChatFormatting.YELLOW), false);
                                                                            src.sendSuccess(() -> Component.translatable("techjs.command.help.unlock").withStyle(ChatFormatting.GREEN), false);
                                                                            src.sendSuccess(() -> Component.translatable("techjs.command.help.add_tech").withStyle(ChatFormatting.GREEN), false);
                                                                            src.sendSuccess(() -> Component.translatable("techjs.command.help.reload_storage").withStyle(ChatFormatting.GRAY), false);
                                                                            src.sendSuccess(() -> Component.translatable("techjs.command.help.apply").withStyle(ChatFormatting.GRAY), false);
                                                                            src.sendSuccess(() -> Component.translatable("techjs.command.help.note").withStyle(ChatFormatting.GRAY), false);
                                    }
                                    return 1;
                                }))
        );
    }
}
