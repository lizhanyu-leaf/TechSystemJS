package com.leaf.techjs;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = TechSystemJS.MOD_ID)
public final class AllConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public static final ForgeConfigSpec SPEC;
    public static final Common COMMON;

    /** 是否注册 /techjs 指令（onLoad 时从配置缓存） */
    public static boolean enableCommands = true;

    /** 指令执行后是否输出反馈消息（onLoad 时从配置缓存） */
    public static boolean commandOutput = true;

    static {
        var pair = BUILDER.configure(Common::new);
        SPEC = pair.getRight();
        COMMON = pair.getLeft();
    }

    public static class Common {

        private final ForgeConfigSpec.BooleanValue enableCommandsValue;
        private final ForgeConfigSpec.BooleanValue commandOutputValue;

        private Common(ForgeConfigSpec.Builder builder) {
            enableCommandsValue = builder
                    .comment("是否注册 /techjs 指令")
                    .define("enableCommands", true);

            commandOutputValue = builder
                    .comment("指令执行后是否输出反馈消息")
                    .define("commandOutput", true);
        }
    }

    @SubscribeEvent
    public static void onLoad(ModConfigEvent event) {
        enableCommands = COMMON.enableCommandsValue.get();
        commandOutput = COMMON.commandOutputValue.get();
    }
}
