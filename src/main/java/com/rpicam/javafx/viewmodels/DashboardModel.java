package com.rpicam.javafx.viewmodels;

import com.rpicam.javafx.App;
import com.rpicam.javafx.util.ViewModel;
import com.rpicam.scenes.SceneInfo;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class DashboardModel implements ViewModel {
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