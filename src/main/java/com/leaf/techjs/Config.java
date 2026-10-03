package com.leaf.techjs;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    public static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /** 是否注册 /techjs 指令 */
    public static final ModConfigSpec.BooleanValue ENABLE_COMMANDS = BUILDER
            .comment("是否注册 /techjs 指令")
            .define("enableCommands", true);

    /** 指令执行后是否输出反馈消息 */
    public static final ModConfigSpec.BooleanValue COMMAND_OUTPUT = BUILDER
            .comment("指令执行后是否输出反馈消息")
            .define("commandOutput", true);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private Config() {
    }
}
