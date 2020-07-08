package com.rpicam.models;

import com.rpicam.video.OCVLocalCamera;
import com.rpicam.video.CameraManager;
import java.util.List;
import java.util.Map;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import com.rpicam.video.CameraWorker;

public class CameraManagerModel {

    private CameraManager cameraManager;
    private SimpleListProperty<CameraModel> selection = new SimpleListProperty<>(FXCollections.observableArrayList());
    private SimpleListProperty<CameraModel> cameraList = new SimpleListProperty<>(FXCollections.observableArrayList());

    public CameraManagerModel(CameraManager aCameraManager) {
        cameraManager = aCameraManager;
    }

    public void updateCameraList(List<CameraModel> aCameraList) {
        cameraList.setAll(aCameraList);
    }

    public CameraWorker addCamera(Map<String, String> params) {
        switch (params.get("type")) {
            case "local" -> {
                var worker = new OCVLocalCamera();
                var workerModel = worker.getModel();
                var options = worker.getOptions();
                options.camIndex = Integer.parseInt(params.get("camIndex"));
                options.api = params.get("api");
                options.resW = Integer.parseInt(params.get("resW"));
                options.resH = Integer.parseInt(params.get("resH"));
                options.capRate = 1000 / Integer.parseInt(params.get("capFPS"));
                options.procRate = 1000 / Integer.parseInt(params.get("procFPS"));
                worker.setOptions(options);
                workerModel.drawDetectionProperty().set(Boolean.parseBoolean(params.get("drawDetection")));
                workerModel.drawStatsProperty().set(Boolean.parseBoolean(params.get("drawStats")));
                try {
                    worker.start();
                    cameraManager.addWorker(worker);
                    return worker;
                }
                catch (Exception ex) {
                    // TODO: Display error dialog
                    ex.printStackTrace();
                    return null;
                }
            }
        }
        return null;
    }

    public void removeSelected() {
        for (var model : selection) {
            cameraManager.removeWorkerViaModel(model);
        }
    }

    public SimpleListProperty<CameraModel> selectionProperty() {
        return selection;
    }

    public SimpleListProperty<CameraModel> cameraListProperty() {
        return cameraList;
    }
}
