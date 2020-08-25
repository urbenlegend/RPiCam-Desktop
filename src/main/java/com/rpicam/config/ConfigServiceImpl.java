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
import com.rpicam.exceptions.ConfigException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

public class ConfigServiceImpl implements ConfigService {
    private static ConfigServiceImpl instance;

    private ConfigRoot configRoot;
    private Gson gson;

    private ConfigServiceImpl() {
        var builder = new GsonBuilder();
        gson = builder.setPrettyPrinting()
                .registerTypeAdapter(CameraConfig.class, new CameraConfigDeserializer())
                .registerTypeAdapter(CameraConfig.class, new CameraConfigSerializer())
                .registerTypeAdapter(ClassifierConfig.class, new ClassifierConfigDeserializer())
                .registerTypeAdapter(ClassifierConfig.class, new ClassifierConfigSerializer())
                .create();
    }

    public static ConfigServiceImpl provider() {
        if (instance == null) {
            instance = new ConfigServiceImpl();
        }
        return instance;
    }

    @Override
    public void loadConfigFile(Path configPath) throws IOException {
        var configStr = Files.readString(configPath, StandardCharsets.US_ASCII);
        try {
            configRoot = gson.fromJson(configStr, ConfigRoot.class);
        } catch (JsonSyntaxException e) {
            throw new ConfigException(configPath.toString() + "is an invalid config file", e);
        }
    }

    @Override
    public void saveConfigFile(Path configPath) throws IOException {
        String configStr = gson.toJson(configRoot);
        Files.writeString(configPath, configStr);
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