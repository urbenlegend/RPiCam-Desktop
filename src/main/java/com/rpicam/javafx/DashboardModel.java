package com.rpicam.javafx;

import com.rpicam.javafx.App;
import com.rpicam.scenes.SceneInfo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class DashboardModel {
    private SimpleObjectProperty<SceneInfo> allCamerasScene = new SimpleObjectProperty<>();

    public DashboardModel() {
        allCamerasScene.set(App.sceneManager().getScene("_ALL_CAMERAS_"));
    }

    public SceneInfo getAllCamerasScene() {
        return allCamerasScene.get();
    }

    public ObjectProperty<SceneInfo> allCamerasSceneProperty() {
        return allCamerasScene;
    }
}