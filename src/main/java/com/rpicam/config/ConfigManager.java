/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author benrx
 */
public class ConfigManager {
    private ConfigRoot configRoot;
    private Gson gson;

    public ConfigManager() {
        var builder = new GsonBuilder();
        gson = builder.setPrettyPrinting()
                .registerTypeAdapter(OCVCameraConfig.class, new CameraDeserializer())
                .registerTypeAdapter(OCVCameraConfig.class, new CameraSerializer())
                .create();
    }

    public void loadConfigFile(Path configPath) throws IOException {
        var configStr = Files.readString(configPath, StandardCharsets.US_ASCII);
        configRoot = gson.fromJson(configStr, ConfigRoot.class);
    }

    public void saveConfigFile(Path configPath) throws IOException {
        String configStr = gson.toJson(configRoot);
        Files.writeString(configPath, configStr);
    }

    public ConfigRoot getConfig() {
        return configRoot;
    }
}

class CameraDeserializer implements JsonDeserializer<OCVCameraConfig> {
    private static Map<String, Class> typeMap = new TreeMap<String, Class>();

    static {
        typeMap.put("", OCVCameraConfig.class);
        typeMap.put("local", OCVLocalCameraConfig.class);
        typeMap.put("path", VlcjCameraConfig.class);
    }

    @Override
    public OCVCameraConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        var jsonObj = json.getAsJsonObject();
        String type = jsonObj.get("type").getAsString();

        Class c = typeMap.get(type);
        if (c == null) {
            throw new RuntimeException("Unknown deserialization class: " + type);
        }

        return context.deserialize(jsonObj, c);
    }
}

class CameraSerializer implements JsonSerializer<OCVCameraConfig> {
    @Override
    public JsonElement serialize(OCVCameraConfig src, Type typeOfSrc,
            JsonSerializationContext context) {
        if (src == null) {
            return null;
        }

        return context.serialize(src, src.getClass());
    }
}