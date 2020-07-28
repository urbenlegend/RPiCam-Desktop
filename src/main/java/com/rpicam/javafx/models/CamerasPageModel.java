package com.rpicam.javafx.models;

import com.rpicam.javafx.App;
import com.rpicam.scenes.SceneInfo;
import com.rpicam.scenes.ViewInfo;
import com.rpicam.cameras.CameraWorker;
import com.rpicam.cameras.OCVLocalCamera;
import com.rpicam.cameras.VlcjCamera;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class CamerasPageModel {
    private SceneInfo scene;
    private SimpleListProperty<ViewInfo> views = new SimpleListProperty<>(FXCollections.observableArrayList());

    public void init(SceneInfo aScene) {
        scene = aScene;
        updateViews();
    }

    public void updateViews() {
        views.setAll(scene.getViews());
    }

    public void addNewCamera(Map<String, String> cameraPropMap) {
        CameraWorker camera = createCamera(cameraPropMap);

        try {
            // Add new camera to camera manager
            camera.start();
            var cameraManager = App.cameraManager();
            UUID cameraUUID = cameraManager.addCamera(camera);

            // Create new view for camera
            var viewInfo = new ViewInfo();
            viewInfo.cameraUUID = cameraUUID;
            viewInfo.drawStats = Boolean.parseBoolean(cameraPropMap.get("drawStats"));
            viewInfo.drawDetection = Boolean.parseBoolean(cameraPropMap.get("drawDetection"));
            scene.addView(viewInfo);
            updateViews();
        }
        catch (Exception ex) {
            // TODO: Display error dialog
            ex.printStackTrace();
        }
    }

    public CameraWorker createCamera(Map<String, String> cameraPropMap) {
        switch (cameraPropMap.get("type")) {
            case "local" -> {
                var newCamera = new OCVLocalCamera();
                var config = newCamera.toConfig();
                config.camIndex = Integer.parseInt(cameraPropMap.get("camIndex"));
                config.captureApi = cameraPropMap.get("captureApi");
                config.widthRes = Integer.parseInt(cameraPropMap.get("widthRes"));
                config.heightRes = Integer.parseInt(cameraPropMap.get("heightRes"));
                config.capRate = 1000 / Integer.parseInt(cameraPropMap.get("capFPS"));
                config.procRate = 1000 / Integer.parseInt(cameraPropMap.get("procFPS"));
                newCamera.fromConfig(config);
                return newCamera;
            }
            case "path" -> {
                var newCamera = new VlcjCamera();
                var config = newCamera.toConfig();
                config.url = cameraPropMap.get("url");
                config.procRate = Integer.parseInt(cameraPropMap.get("procFPS"));
                newCamera.fromConfig(config);
                return newCamera;
            }
        }

        return null;
    }

    public List<ViewInfo> getViews() {
        return views.get();
    }

    public ReadOnlyListProperty<ViewInfo> viewsProperty() {
        return views;
    }
}
