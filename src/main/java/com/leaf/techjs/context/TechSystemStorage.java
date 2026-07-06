package com.leaf.techjs.context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

public final class TechSystemStorage {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "tech_system.json";

    private final Map<TechInfo, Boolean> technologies = new HashMap<>();
    private final Path filePath;

    private static final Queue<Runnable> whenCreate = new LinkedList<>();

    private static TechSystemStorage INSTANCE;
    public static MinecraftServer SERVER;

    private TechSystemStorage(Path worldDir) {
        this.filePath = worldDir.resolve(FILE_NAME);
        load();
    }

    public static void whenCreate(Runnable runnable) {
        if (INSTANCE != null) {
            runnable.run();
            return;
        }
        whenCreate.add(runnable);
    }

    public static void whenCreate(Consumer<TechSystemStorage> consumer) {
        whenCreate.add(() -> consumer.accept(INSTANCE));
    }

    public static void whenCreateWithServer(Consumer<MinecraftServer> consumer) {
        whenCreate.add(() -> consumer.accept(SERVER));
    }

    public static TechSystemStorage getOrCreateInstance(MinecraftServer server) {
        SERVER = server;
        if (INSTANCE == null) {
            // 用主世界的存档目录
            ServerLevel overworld = server.overworld();
            Path worldDir = overworld.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
            INSTANCE = new TechSystemStorage(worldDir);

            while  (!whenCreate.isEmpty()) {
                Runnable runnable = whenCreate.poll();
                runnable.run();
            }
        }
        return INSTANCE;
    }

    public static TechSystemStorage getInstance() {
        return INSTANCE;
    }

    public static void reset() {
        INSTANCE = null;
    }

    // ========== 读写 ==========

    public void load() {
        technologies.clear();
        if (!Files.exists(filePath)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(filePath)) {
            Map<String, Boolean> data = GSON.fromJson(reader, new TypeToken<Map<String, Boolean>>() {}.getType());
            if (data != null) {
                for (Map.Entry<String, Boolean> entry : data.entrySet()) {
                    technologies.put(TechInfo.of(entry.getKey()), entry.getValue());
                }
            }
        } catch (IOException e) {
            System.err.println("[TechnologyStorage] 加载失败: " + e.getMessage());
        }
    }

    public void save() {
        try {
            Files.createDirectories(filePath.getParent());
            try (Writer writer = Files.newBufferedWriter(filePath)) {
                GSON.toJson(technologies, writer);
            }
        } catch (IOException e) {
            System.err.println("[TechnologyStorage] 保存失败: " + e.getMessage());
        }
    }

    // ========== 操作 ==========

    public boolean isActive(String techId) {
        return isActive(TechInfo.of(techId));
    }

    public boolean isActive(TechInfo techInfo) {
        return technologies.getOrDefault(techInfo, false);
    }

    public void setActive(String techId, boolean active) {
        ResourceLocation id = ResourceLocation.tryParse(techId);
        if (id == null) return;
        TechInfo techInfo = TechInfo.of(id);
        setActive(techInfo, active);
    }

    public void setActive(TechInfo techInfo, boolean active) {
        technologies.put(techInfo, active);
        if (technologies.getOrDefault(techInfo, false) == active) return;
        TechSystem.setDirty();
        save(); // 立即保存
    }

    public Map<TechInfo, Boolean> getAll() {
        return new HashMap<>(technologies);
    }

    public List<TechInfo> getAllActive() {
        List<TechInfo> list = new ArrayList<>();
        for (Map.Entry<TechInfo, Boolean> entry : technologies.entrySet()) {
            if (entry.getValue()) {
                list.add(entry.getKey());
            }
        }
        return list;
    }

    public List<TechInfo> getAllTechs() {
        return new ArrayList<>(technologies.keySet());
    }

    // 获取文件路径
    public Path getFilePath() {
        return filePath;
    }
}
