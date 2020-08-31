package com.rpicam.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import static com.rpicam.Constants.APP_NAME;
import static com.rpicam.Constants.CONFIG_FILE_NAME;
import static com.rpicam.Constants.DEFAULT_CONFIG_PATH;
import com.rpicam.exceptions.ConfigException;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;

public class ConfigServiceImpl implements ConfigService {
    private static ConfigServiceImpl instance;

    private Gson gson;
    private ConfigRoot configRoot;
    private Path configPath;

    private ConfigServiceImpl() {
        var builder = new GsonBuilder();
        gson = builder.setPrettyPrinting()
                .registerTypeAdapter(CameraConfig.class, new CameraConfigDeserializer())
                .registerTypeAdapter(CameraConfig.class, new CameraConfigSerializer())
                .registerTypeAdapter(ClassifierConfig.class, new ClassifierConfigDeserializer())
                .registerTypeAdapter(ClassifierConfig.class, new ClassifierConfigSerializer())
                .create();

        // Detect configuration path based on OS
        if (System.getProperty("os.name").startsWith("Windows")) {
            configPath = Paths.get(String.format("%s\\%s\\%s", System.getenv("APPDATA"), APP_NAME, CONFIG_FILE_NAME));
        } else {
            configPath = Paths.get(String.format("%s/.%s/%s", System.getProperty("user.home"), APP_NAME.toLowerCase(), CONFIG_FILE_NAME));
        }

        // Load config file if it exists, otherwise load defaults
        if (configPath.toFile().exists()) {
            loadConfigFile(configPath);
        }
        else {
            loadConfigFile(Paths.get(DEFAULT_CONFIG_PATH));
        }
    }

    public static ConfigServiceImpl provider() {
        if (instance == null) {
            instance = new ConfigServiceImpl();
        }
        return instance;
    }

    @Override
    public void shutdown() {
        // Before saving, make application config directory if it doesn't exist
        File configDir = configPath.getParent().toFile();
        if (!configDir.exists()) {
            configDir.mkdir();
        }
        saveConfigFile(configPath);
    }

    @Override
    public void loadConfigFile(Path configPath) {
        try {
            var configStr = Files.readString(configPath, StandardCharsets.US_ASCII);
            configRoot = gson.fromJson(configStr, ConfigRoot.class);
        } catch (JsonSyntaxException e) {
            throw new ConfigException(configPath.toString() + "is an invalid config file", e);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void saveConfigFile(Path configPath) {
        try {
            String configStr = gson.toJson(configRoot);
            Files.writeString(configPath, configStr);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public ConfigRoot getConfig() {
        return configRoot;
    }
}

class CameraConfigDeserializer implements JsonDeserializer<CameraConfig> {
    private static Map<String, Class> typeMap = new TreeMap<String, Class>();

    static {
        typeMap.put("", CameraConfig.class);
        typeMap.put("local", OCVLocalCameraConfig.class);
        typeMap.put("path", VlcjCameraConfig.class);
    }

    @Override
    public CameraConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        var jsonObj = json.getAsJsonObject();
        String type = jsonObj.get("type").getAsString();

        Class c = typeMap.get(type);
        if (c == null) {
            throw new RuntimeException("Unknown deserialization class: " + type);
        }

        return context.deserialize(jsonObj, c);
    }
}

class CameraConfigSerializer implements JsonSerializer<CameraConfig> {
    @Override
    public JsonElement serialize(CameraConfig src, Type typeOfSrc,
            JsonSerializationContext context) {
        if (src == null) {
            return null;
        }

        return context.serialize(src, src.getClass());
    }
}

class ClassifierConfigDeserializer implements JsonDeserializer<ClassifierConfig> {
    private static Map<String, Class> typeMap = new TreeMap<String, Class>();

    static {
        typeMap.put("", ClassifierConfig.class);
        typeMap.put("opencv", OCVClassifierConfig.class);
    }

    @Override
    public ClassifierConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        var jsonObj = json.getAsJsonObject();
        String type = jsonObj.get("type").getAsString();

        Class c = typeMap.get(type);
        if (c == null) {
            throw new RuntimeException("Unknown deserialization class: " + type);
        }

        return context.deserialize(jsonObj, c);
    }
}

class ClassifierConfigSerializer implements JsonSerializer<ClassifierConfig> {
    @Override
    public JsonElement serialize(ClassifierConfig src, Type typeOfSrc,
            JsonSerializationContext context) {
        if (src == null) {
            return null;
        }

        return context.serialize(src, src.getClass());
    }
}
