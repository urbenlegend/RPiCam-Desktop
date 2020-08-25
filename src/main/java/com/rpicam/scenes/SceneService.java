package com.rpicam.scenes;

public interface SceneService {
    void shutdown();
    void addScene(String title, SceneInfo scene);
    void removeScene(String title);
    void clearScenes();
    SceneInfo getScene(String title);
}
