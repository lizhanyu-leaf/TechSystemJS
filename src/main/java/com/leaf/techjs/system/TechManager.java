package com.leaf.techjs.system;

import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.leaf.techjs.TechSystemJS;
import com.leaf.techjs.kubejs.TechSystemEvents;
import com.leaf.techjs.kubejs.event.TechEventJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class TechManager {

    private static final String FILE_NAME = "tech_system.json";
    private static final Type STATE_TYPE = new TypeToken<Map<String, Boolean>>() {}.getType();

    private static final Map<ResourceLocation, Boolean> isEnable = new HashMap<>();
    private static final Map<ResourceLocation, Tech> techs = new HashMap<>();

    /** 存档目录下的持久化文件，仅在服务器存在时可用 */
    private static @Nullable Path filePath;

    public static void toggle(ResourceLocation id, boolean isEnable) {
        // 状态未变化时是 no-op
        if (TechManager.isEnable.getOrDefault(id, false) == isEnable) return;

        TechManager.isEnable.put(id, isEnable);
        save();
        TechRecipeManager.update(id);

        // 调用 JS 端 OnTechEnable / OnTechDisable，这两个事件在监听时都需要指明 Tech 的 ResourceLocation
        TechEventJS event = new TechEventJS(id, techs.get(id));
        if (isEnable) {
            TechSystemEvents.ON_TECH_ENABLE.post(ScriptType.SERVER, id, event);
        } else {
            TechSystemEvents.ON_TECH_DISABLE.post(ScriptType.SERVER, id, event);
        }
    }

    public static boolean isEnable(ResourceLocation id) {
        return isEnable.getOrDefault(id, false);
    }

    /** 仅更新状态，不触发事件 / 配方更新（客户端同步镜像用） */
    static void setState(ResourceLocation id, boolean enable) {
        isEnable.put(id, enable);
    }

    @Nullable
    public static Tech getTech(ResourceLocation id) {
        return techs.get(id);
    }

    public static Tech register(Tech tech) {
        // 每个注册的科技都在存档里写一个默认 false，便于在文件中查看与手动修改
        if (isEnable.putIfAbsent(tech.id(), false) == null) {
            save();
        }
        return techs.put(tech.id(), tech);
    }

    public static List<Tech> registerAll(List<Tech> techs) {
        techs.forEach(TechManager::register);
        return techs;
    }

    public static Set<Tech> getAllTechs() {
        return new HashSet<>(techs.values());
    }

    /** 已知的科技启用状态（只读），用于向客户端同步 */
    public static Map<ResourceLocation, Boolean> getStates() {
        return Collections.unmodifiableMap(isEnable);
    }

    /** 所有已注册或已有状态的科技 id（指令补全用） */
    public static Set<ResourceLocation> getAllTechIds() {
        Set<ResourceLocation> ids = new LinkedHashSet<>(techs.keySet());
        ids.addAll(isEnable.keySet());
        return ids;
    }

    // ========== 持久化 ==========

    /** 服务器启动 / /reload 时从存档目录恢复科技状态，需在脚本注册科技之前调用 */
    public static void load(MinecraftServer server) {
        isEnable.clear();
        techs.clear();
        filePath = server.getWorldPath(LevelResource.ROOT).resolve(FILE_NAME);
        if (!Files.exists(filePath)) {
            TechSystemJS.LOGGER.info("[TechManager] : {} 不存在，跳过加载", filePath);
            return;
        }

        try (Reader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            Map<String, Boolean> data = TechSystemJS.GSON.fromJson(reader, STATE_TYPE);
            if (data == null) return;
            data.forEach((key, value) -> {
                ResourceLocation id = ResourceLocation.tryParse(key);
                if (id != null) isEnable.put(id, value);
            });
            TechSystemJS.LOGGER.info("[TechManager] : 已加载 {} 条科技状态", isEnable.size());
        } catch (IOException | JsonParseException | IllegalStateException e) {
            TechSystemJS.LOGGER.error("[TechManager] : 加载 {} 失败: {}", filePath, e.getMessage());
        }
    }

    /** 立即保存科技状态到存档目录 */
    public static void save() {
        if (filePath == null) return;

        Map<String, Boolean> data = new LinkedHashMap<>();
        isEnable.forEach((id, value) -> data.put(id.toString(), value));

        try {
            Files.createDirectories(filePath.getParent());
            try (Writer writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
                TechSystemJS.GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            TechSystemJS.LOGGER.error("[TechManager] : 保存 {} 失败: {}", filePath, e.getMessage());
        }
    }
}
