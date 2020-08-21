package com.rpicam.javafx.viewmodels;

import com.rpicam.Constants;
import com.rpicam.javafx.util.ViewModel;
import com.rpicam.scenes.SceneInfo;
import com.rpicam.scenes.ViewInfo;
import com.rpicam.cameras.CameraWorker;
import com.rpicam.cameras.OCVLocalCamera;
import com.rpicam.cameras.VlcjCamera;
import com.rpicam.javafx.App;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import org.apache.commons.lang3.math.NumberUtils;

public class CamerasPageModel implements ViewModel {
    private SceneInfo scene;

    private SimpleListProperty<ViewInfo> views = new SimpleListProperty<>(FXCollections.observableArrayList());
    private PropertyChangeListener scenePropertyListener;

    public CamerasPageModel() {
        scene = App.sceneManager().getScene("_ALL_CAMERAS_");
        scenePropertyListener = event -> {
            views.setAll(scene.getViews());
        };
    }

    @Override
    public void onViewAdded() {
        views.setAll(scene.getViews());
        scene.addPropertyChangeListener("views", scenePropertyListener);
    }

    @Override
    public void onViewRemoved() {
        scene.removePropertyChangeListener("views", scenePropertyListener);
    }

    public List<ViewInfo> getViews() {
        return views.get();
    }

    public ReadOnlyListProperty<ViewInfo> viewsProperty() {
        return views;
    }

    public void addNewCamera(Map<String, String> cameraPropMap) {
        // Add new camera to camera manager
        CameraWorker camera = createCamera(cameraPropMap);
        UUID cameraUUID = App.cameraManager().addCamera(camera);

        // Create new view for camera
        var viewInfo = new ViewInfo();
        viewInfo.cameraUUID = cameraUUID;
        viewInfo.drawStats = Boolean.parseBoolean(cameraPropMap.get("drawStats"));
        viewInfo.drawDetection = Boolean.parseBoolean(cameraPropMap.get("drawDetection"));
        scene.addView(viewInfo);

        camera.start();
    }

    public CameraWorker createCamera(Map<String, String> cameraPropMap) {
        switch (cameraPropMap.get("type")) {
            case "local" -> {
                var newCamera = new OCVLocalCamera();
                var config = newCamera.toConfig();
                config.camIndex = Integer.parseInt(cameraPropMap.get("camIndex"));
                config.captureApi = cameraPropMap.get("captureApi");
                config.widthRes = NumberUtils.toInt(cameraPropMap.get("widthRes"), 0);
                config.heightRes = NumberUtils.toInt(cameraPropMap.get("heightRes"), 0);
                config.capRate = 1000 / NumberUtils.toInt(cameraPropMap.get("capRate"), Constants.CAP_RATE_DEFAULT);
                config.procInterval = NumberUtils.toInt(cameraPropMap.get("procInterval"), Constants.PROC_INTERVAL_DEFAULT);
                newCamera.fromConfig(config);
                return newCamera;
            }
            case "path" -> {
                var newCamera = new VlcjCamera();
                var config = newCamera.toConfig();
                config.url = cameraPropMap.get("url");
                config.procInterval = NumberUtils.toInt(cameraPropMap.get("procInterval"), Constants.PROC_INTERVAL_DEFAULT);
                newCamera.fromConfig(config);
                return newCamera;
            }
        }

        return null;
    }

    public void removeCameraByViewInfo(ViewInfo view) {
        scene.removeView(view);
        var cameraManager = App.cameraManager();
        cameraManager.removeCamera(view.cameraUUID);
    }
}
