package com.rpicam.scenes;

import com.rpicam.config.SceneConfig;
import com.rpicam.ui.App;
import java.util.HashMap;
import java.util.stream.Collectors;

public class SceneManager {
    private HashMap<String, SceneInfo> scenes = new HashMap<>();

    public void addScene(String title, SceneInfo scene) {
        scenes.put(title, scene);
    }

    public void removeScene(String title) {
        scenes.remove(title);
    }

    public SceneInfo getScene(String title) {
        var scene = scenes.get(title);
        if (scene == null) {
            scene = new SceneInfo();
            addScene(title, scene);
        }
        return scene;
    }

    public void loadConfig() {
        for (var sceneConfig : App.getConfigManager().getConfig().scenes) {
            var scene = new SceneInfo();
            scene.fromConfig(sceneConfig);
            addScene(sceneConfig.title, scene);
        }
    }

    public void saveConfig() {
        var configRoot = App.getConfigManager().getConfig();
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
}
