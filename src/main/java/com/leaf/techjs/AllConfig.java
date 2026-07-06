package com.leaf.techjs;

import com.leaf.techjs.context.TechSystem;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = TechSystemJS.MOD_ID)
public final class AllConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec SPEC;
    public static final Common COMMON;

    static {
        var pair = BUILDER.configure(Common::new);
        SPEC = pair.getRight();
        COMMON = pair.getLeft();
    }

    public static class Common {
        public final ForgeConfigSpec.LongValue RESTART_DELAY;
        public final ForgeConfigSpec.BooleanValue ENABLE_COMMANDS;
        public final ForgeConfigSpec.BooleanValue ENABLE_COMMANDS_TIPS;

        private Common(ForgeConfigSpec.Builder builder) {
            RESTART_DELAY = builder.comment("科技系统重启延迟（毫秒）")
                    .defineInRange("restartDelay", 2000, 50, Long.MAX_VALUE);

            ENABLE_COMMANDS = builder.comment("是否启用科技系统命令")
                    .define("enableCommands", true);

            ENABLE_COMMANDS_TIPS = builder.comment("是否启用科技系统命令提示")
                    .define("enableCommandsTips", true);
        }
    }

    public static long restartDelay;
    public static boolean enableCommands;
    public static boolean enableCommandsTips;

    @SubscribeEvent
    public static void onLoad(ModConfigEvent event) {
        restartDelay = COMMON.RESTART_DELAY.get();
        enableCommands = COMMON.ENABLE_COMMANDS.get();
        enableCommandsTips = COMMON.ENABLE_COMMANDS_TIPS.get();

        TechSystem.init();
    }
}
