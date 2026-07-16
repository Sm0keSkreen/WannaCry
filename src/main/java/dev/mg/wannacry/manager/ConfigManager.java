package dev.mg.wannacry.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dev.mg.wannacry.WannaCry;
import dev.mg.wannacry.features.Feature;
import dev.mg.wannacry.features.settings.Bind;
import dev.mg.wannacry.features.settings.EnumConverter;
import dev.mg.wannacry.features.settings.Setting;
import dev.mg.wannacry.util.traits.Jsonable;
import net.fabricmc.loader.api.FabricLoader;
import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("ConfigManager");
    private static final Path SATELLITE_PATH = FabricLoader.getInstance().getGameDir().resolve("wannacry");
    private static final Path CONFIGS_PATH   = SATELLITE_PATH.resolve("configs");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final List<Jsonable> jsonables = new LinkedList<>();

    public void addConfig(Jsonable jsonable) {
        jsonables.add(jsonable);
    }

    public void load() {
        mkdirs();
        for (Jsonable jsonable : jsonables) {
            try {
                String read = Files.readString(SATELLITE_PATH.resolve(jsonable.getFileName()));
                jsonable.fromJson(JsonParser.parseString(read));
            } catch (Throwable e) {
                LOGGER.error("Failed to load", e);
            }
        }
    }

    public void save() {
        mkdirs();
        for (Jsonable jsonable : jsonables) {
            try {
                JsonElement json = jsonable.toJson();
                Files.writeString(SATELLITE_PATH.resolve(jsonable.getFileName()), GSON.toJson(json));
            } catch (Throwable e) {
                LOGGER.error("Failed to write to file", e);
            }
        }
    }

    public void saveConfig(String name) {
        mkdirs();
        mkConfigsDir();
        try {
            com.google.gson.JsonObject root = new com.google.gson.JsonObject();
            for (Jsonable jsonable : jsonables) {
                root.add(jsonable.getFileName(), jsonable.toJson());
            }
            String filename = sanitize(name) + ".json";
            Files.writeString(CONFIGS_PATH.resolve(filename), GSON.toJson(root));
            LOGGER.info("Saved config '{}'", name);
        } catch (Throwable e) {
            LOGGER.error("Failed to save config '{}'", name, e);
        }
    }

    public void loadConfig(String name) {
        String filename = sanitize(name) + ".json";
        Path file = CONFIGS_PATH.resolve(filename);
        if (!file.toFile().exists()) {
            LOGGER.warn("Config '{}' not found", name);
            return;
        }
        try {
            String read = Files.readString(file);
            com.google.gson.JsonObject root = JsonParser.parseString(read).getAsJsonObject();
            for (Jsonable jsonable : jsonables) {
                JsonElement el = root.get(jsonable.getFileName());
                if (el != null && !el.isJsonNull()) {
                    jsonable.fromJson(el);
                }
            }
            LOGGER.info("Loaded config '{}'", name);
        } catch (Throwable e) {
            LOGGER.error("Failed to load config '{}'", name, e);
        }
    }

    public void deleteConfig(String name) {
        String filename = sanitize(name) + ".json";
        Path file = CONFIGS_PATH.resolve(filename);
        try {
            Files.deleteIfExists(file);
            LOGGER.info("Deleted config '{}'", name);
        } catch (Throwable e) {
            LOGGER.error("Failed to delete config '{}'", name, e);
        }
    }

    public List<String> listConfigs() {
        mkConfigsDir();
        File[] files = CONFIGS_PATH.toFile().listFiles((d, n) -> n.endsWith(".json"));
        if (files == null) return new ArrayList<>();
        return Arrays.stream(files)
                .map(f -> f.getName().replace(".json", ""))
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    private void mkdirs() {
        if (!SATELLITE_PATH.toFile().exists()) {
            boolean success = SATELLITE_PATH.toFile().mkdirs();
            if (!success) throw new RuntimeException("Failed to create needed directories!");
        }
    }

    private void mkConfigsDir() {
        if (!CONFIGS_PATH.toFile().exists()) {
            CONFIGS_PATH.toFile().mkdirs();
        }
    }

    private static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9 _\\-]", "_").trim();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void setValueFromJson(Feature feature, Setting setting, JsonElement element) {
        if (element == null || element.isJsonNull()) return;
        switch (setting.getType()) {
            case "Boolean" -> setting.setValue(element.getAsBoolean());
            case "Double"  -> setting.setValue(element.getAsDouble());
            case "Float"   -> setting.setValue(element.getAsFloat());
            case "Integer" -> setting.setValue(element.getAsInt());
            case "String"  -> setting.setValue(element.getAsString());
            case "Bind"    -> setting.setValue(new Bind(element.getAsInt()));
            case "Color"   -> {
                try {
                    String colorStr = element.getAsString();
                    String[] parts = colorStr.split(",");
                    if (parts.length == 4) {
                        int r = Integer.parseInt(parts[0]);
                        int g = Integer.parseInt(parts[1]);
                        int b = Integer.parseInt(parts[2]);
                        int a = Integer.parseInt(parts[3]);
                        setting.setValue(new Color(r, g, b, a));
                    }
                } catch (Exception exception) {
                    LOGGER.error("Error parsing color for: {} : {}", feature.getName(), setting.getName());
                }
            }
            case "Pos" -> {
                try {
                    String posStr = element.getAsString();
                    String[] parts = posStr.split(",");
                    if (parts.length == 2) {
                        float x = Float.parseFloat(parts[0]);
                        float y = Float.parseFloat(parts[1]);
                        setting.setValue(new Vector2f(x, y));
                    }
                } catch (Exception exception) {
                    LOGGER.error("Error parsing position for: {} : {}", feature.getName(), setting.getName());
                }
            }
            case "Enum" -> {
                try {
                    EnumConverter converter = new EnumConverter(setting.getValue().getClass());
                    Enum value = converter.doBackward(element);
                    setting.setValue(value);
                } catch (Exception exception) {
                    LOGGER.error("Error parsing enum for {}.{}: {}", feature.getName(), setting.getName(), exception);
                }
            }
            default -> LOGGER.error("Unknown Setting type for: {} : {}", feature.getName(), setting.getName());
        }
    }
}
