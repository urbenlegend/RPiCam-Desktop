package com.rpicam.scenes;

import com.rpicam.config.ConfigService;
import com.rpicam.config.SceneConfig;
import java.util.HashMap;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public class SceneServiceImpl implements SceneService {
    private static SceneServiceImpl instance;

    private ConfigService configService;
    
    private HashMap<String, SceneInfo> scenes = new HashMap<>();

    private SceneServiceImpl () {
        configService = ServiceLoader.load(ConfigService.class).findFirst().get();
        for (var sceneConfig : configService.getConfig().scenes) {
            var scene = new SceneInfo();
            scene.fromConfig(sceneConfig);
            addScene(sceneConfig.title, scene);
        }
    }

    public static SceneServiceImpl provider() {
        if (instance == null) {
            instance = new SceneServiceImpl();
        }
        return instance;
    }

    @Override
    public void shutdown() {
        var configRoot = configService.getConfig();
        configRoot.scenes = new SceneConfig[scenes.size()];
        scenes.entrySet().stream()
                .map((entry) -> {
                    var scene = entry.getValue();
                    var sceneConfig = scene.toConfig();
                    sceneConfig.title = entry.getKey();
                    return sceneConfig;
                })
                .collect(Collectors.toList())
                .toArray(configRoot.scenes);
    }

    @Override
    public void addScene(String title, SceneInfo scene) {
        scenes.put(title, scene);
    }

    @Override
    public void removeScene(String title) {
        scenes.remove(title);
    }

    @Override
    public void clearScenes() {
        scenes.clear();
    }

    @Override
    public SceneInfo getScene(String title) {
        var scene = scenes.get(title);
        if (scene == null) {
            scene = new SceneInfo();
            addScene(title, scene);
        }
        return scene;
    }
}
