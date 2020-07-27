package com.rpicam.cameras;

import com.rpicam.detection.OCVClassifier;
import com.rpicam.config.OCVCameraConfig;
import com.rpicam.config.OCVClassifierConfig;
import com.rpicam.config.OCVLocalCameraConfig;
import com.rpicam.config.OCVStreamCameraConfig;
import com.rpicam.javafx.App;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class CameraManager {
    private HashMap<UUID, CameraWorker> cameras = new HashMap<>();
    private ArrayList<OCVClassifier> classifiers = new ArrayList<>();

    public void loadConfig() {
        var configRoot = App.configManager().getConfig();
        for (var conf : configRoot.classifiers) {
            var classifier = new OCVClassifier(conf.path, conf.title, conf.color);
            classifiers.add(classifier);
        }

        for (var conf : configRoot.cameras) {
            CameraWorker newCamera = null;
            // TODO: Add other camera types
            if (conf instanceof OCVLocalCameraConfig) {
                newCamera = new OCVLocalCamera();
            }
            else if (conf instanceof OCVStreamCameraConfig) {
                newCamera = new OCVStreamCamera();
            }

            newCamera.fromConfig(conf);
            addCamera(newCamera, UUID.fromString(conf.uuid));
        }
    }

    public void saveConfig() {
        var configRoot = App.configManager().getConfig();

        ArrayList<OCVClassifierConfig> classifierConfs = new ArrayList<>();
        for (var classifier : classifiers) {
            classifierConfs.add(classifier.toConfig());
        }
        configRoot.classifiers = new OCVClassifierConfig[classifierConfs.size()];
        classifierConfs.toArray(configRoot.classifiers);

        ArrayList<OCVCameraConfig> cameraConfs = new ArrayList<>();
        for (var entry : cameras.entrySet()) {
            var cameraUUID = entry.getKey();
            var camera = entry.getValue();

            var conf = camera.toConfig();
            conf.uuid = cameraUUID.toString();
            cameraConfs.add(conf);
        }
        configRoot.cameras = new OCVCameraConfig[cameraConfs.size()];
        cameraConfs.toArray(configRoot.cameras);
    }

    public void startCameras() {
        for (var c : cameras.values()) {
            c.start();
        }
    }

    public void stopCameras() {
        for (var c : cameras.values()) {
            c.stop();
        }
    }

    public UUID addCamera(CameraWorker camera) {
        UUID cameraUUID = UUID.randomUUID();
        addCamera(camera, cameraUUID);
        return cameraUUID;
    }

    public void addCamera(CameraWorker camera, UUID cameraUUID) {
        // TODO: Move classifier adding elsewhere
        if (camera instanceof OCVLocalCamera) {
            var ocvCamera = (OCVLocalCamera) camera;
            for (var c : classifiers) {
                ocvCamera.addClassifier(c);
            }
        }

        cameras.put(cameraUUID, camera);
    }

    public void removeCamera(UUID cameraUUID) {
        CameraWorker camera = cameras.get(cameraUUID);
        camera.stop();
        cameras.remove(cameraUUID);
    }

    public CameraWorker getCamera(UUID cameraUUID) {
        return cameras.get(cameraUUID);
    }
}
