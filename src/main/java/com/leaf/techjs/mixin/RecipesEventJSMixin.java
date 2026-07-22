package com.leaf.techjs.mixin;

import com.google.common.base.Stopwatch;
import com.leaf.techjs.TechSystemJS;
import com.leaf.techjs.context.TechSystemStorage;
import com.leaf.techjs.kubejs.TechSystemEvents;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.ConsoleJS;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipesEventJS.class)
public class RecipesEventJSMixin {
    @Inject(method = "post",
        remap = false,
        at = @At(
                value = "INVOKE",
                target = "Ldev/latvian/mods/kubejs/event/EventHandler;post(Ldev/latvian/mods/kubejs/script/ScriptTypeHolder;Ldev/latvian/mods/kubejs/event/EventJS;)Ldev/latvian/mods/kubejs/event/EventResult;",
                shift = At.Shift.AFTER))
    private void afterRecipesEvent(CallbackInfo ci) {
        RecipesEventJS instance = (RecipesEventJS) (Object) this;
        TechSystemJS.LOGGER.info("TechSystemJS : TechSystem Loaded");

        if (TechSystemStorage.getInstance() == null) return;
        TechSystemStorage storage = TechSystemStorage.getInstance();

        // 创建计时器
        var techTimer = Stopwatch.createStarted();

        // 获取所有启用科技
        var activeTechs = storage.getAllActive();
        ConsoleJS.SERVER.info("Posting tech recipes event...");

        // 遍历所有启用科技
        for (var techInfo : activeTechs) {
            // 检查是否有对应监听
            if (!TechSystemEvents.ON_TECH_LOAD.hasListeners(techInfo)) {
                ConsoleJS.SERVER.debug("Skipping " + techInfo.id + " (no listeners)");
                continue;
            }

            // 触发事件
            ConsoleJS.SERVER.debug("Posting tech recipes event for " + techInfo.id);
            TechSystemEvents.ON_TECH_LOAD.post(
                    ScriptType.SERVER, techInfo, instance);
        }
        ConsoleJS.SERVER.info("Posted tech recipes event in " + techTimer.stop());
    }
}
