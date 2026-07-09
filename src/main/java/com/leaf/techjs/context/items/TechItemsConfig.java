package com.leaf.techjs.context.items;

import com.google.common.reflect.TypeToken;
import com.leaf.techjs.TechSystemJS;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static com.leaf.techjs.TechSystemJS.GSON;

public final class TechItemsConfig {
    public static final Set<TechItemInfo> items = new HashSet<>();
    public static Map<String, Map<String, String>> data = new HashMap<>();
    private static boolean loaded = false;

    private static final String CONFIG_FILE_NAME = "tech_items_config.json";
    private static final Path CONFIG_DIRECTORY = Path.of("TechSystemJS");

    public static void load() {
        if (loaded) return;
        loaded = true;

        TechSystemJS.LOGGER.info("[TechItemsConfig] : 正在读取配置文件");
        Path configPath = FMLPaths.CONFIGDIR.get().resolve(CONFIG_DIRECTORY).resolve(CONFIG_FILE_NAME);

        try {
            Files.createDirectories(configPath.getParent());
            if (!Files.exists(configPath)) {
                Files.createFile(configPath);
                Files.write(configPath, "{}".getBytes());
                return;
            }

            Map<String, Map<String, String>> data;

            try (Reader reader = Files.newBufferedReader(configPath)) {
                data = TechSystemJS.GSON.fromJson(reader,
                        new TypeToken<Map<String, Map<String, String>>>() {}.getType());
                TechItemsConfig.data = data;
            } catch (Exception e) {
                data = new HashMap<>();
                TechSystemJS.LOGGER.error("[TechItemsConfig] : 读取配置文件失败");
            }

            for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
                String itemId = entry.getValue().containsKey("item_id") ?
                        entry.getValue().get("item_id") : entry.getKey().split(":")[1] + "_unlocker";

                TechItemInfo item = TechItemInfo
                        .of(itemId, entry.getKey(), entry.getValue().get("icon"));

                if (item == null) continue;
                items.add(item);
            }
        } catch (Exception e) {
            TechSystemJS.LOGGER.error("[TechItemsConfig] : 读取配置失败");
        }
    }

    public static void saveData(Map<String, Map<String, String>> data) {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve(CONFIG_DIRECTORY).resolve(CONFIG_FILE_NAME);
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            TechItemsConfig.data.putAll(data);
            GSON.toJson(TechItemsConfig.data, writer);
        } catch (IOException e) {
            TechSystemJS.LOGGER.error("[TechItemsConfig] : 保存配置文件失败");
        }
    }

    public static void setTechItems(List<TechItemInfo> techItems) {
        items.addAll(techItems);
        loaded = true;
    }

    public static void reset() {
        loaded = false;
    }
}
