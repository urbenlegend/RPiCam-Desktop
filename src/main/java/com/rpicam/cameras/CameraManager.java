package com.rpicam.cameras;

import com.rpicam.detection.OCVClassifier;
import com.rpicam.config.OCVCameraConfig;
import com.rpicam.config.OCVClassifierConfig;
import com.rpicam.config.OCVLocalCameraConfig;
import com.rpicam.config.VlcjCameraConfig;
import com.rpicam.javafx.App;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CameraManager {
    private HashMap<UUID, CameraWorker> cameras = new HashMap<>();
    private ArrayList<OCVClassifier> classifiers = new ArrayList<>();

    public void loadConfig() {
        var configRoot = App.configManager().getConfig();
        for (var conf : configRoot.classifiers) {
            var classifier = new OCVClassifier(conf.path, conf.title, conf.color, conf.scaleFactor, conf.minNeighbors, conf.minSizeFactor, conf.gpu);
            classifiers.add(classifier);
        }

        for (var conf : configRoot.cameras) {
            CameraWorker newCamera = null;
            // TODO: Add other camera types
            if (conf instanceof OCVLocalCameraConfig) {
                newCamera = new OCVLocalCamera();
            }
            else if (conf instanceof VlcjCameraConfig) {
                newCamera = new VlcjCamera();
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
            try {
                c.start();
            } catch (Throwable t) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Camera failed to start", t);
            }
        }
    }

    public void stopCameras() {
        for (var c : cameras.values()) {
            try {
                c.stop();
            } catch (Throwable t) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Camera failed to stop", t);
            }
        }
    }

    public UUID addCamera(CameraWorker camera) {
        var cameraUUID = UUID.randomUUID();
        addCamera(camera, cameraUUID);
        return cameraUUID;
    }

    public void addCamera(CameraWorker camera, UUID cameraUUID) {
        // TODO: Consider moving classifier adding elsewhere
        for (var c : classifiers) {
            camera.addClassifier(c.clone());
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
