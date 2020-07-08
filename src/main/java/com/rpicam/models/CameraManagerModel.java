package com.rpicam.models;

import com.rpicam.video.CameraManager;
import java.util.List;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import com.rpicam.video.CameraWorker;

public class CameraManagerModel {

    private CameraManager cameraManager;
    private SimpleListProperty<CameraWorker> selection = new SimpleListProperty<>(FXCollections.observableArrayList());
    private SimpleListProperty<CameraWorker> cameraList = new SimpleListProperty<>(FXCollections.observableArrayList());

    public CameraManagerModel(CameraManager aCameraManager) {
        cameraManager = aCameraManager;
    }

    public void updateCameraList(List<CameraWorker> aCameraList) {
        cameraList.setAll(aCameraList);
    }

    public void addCamera(CameraWorker camera) {
        cameraManager.addCamera(camera);
    }

    public void removeSelected() {
        for (var camera : selection) {
            cameraManager.removeCamera(camera);
        }
    }

    public SimpleListProperty<CameraWorker> selectionProperty() {
        return selection;
    }

    public SimpleListProperty<CameraWorker> cameraListProperty() {
        return cameraList;
    }
}
