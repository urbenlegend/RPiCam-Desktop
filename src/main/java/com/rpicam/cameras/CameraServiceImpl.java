package com.rpicam.cameras;

import com.rpicam.config.CameraConfig;
import com.rpicam.config.ConfigService;
import com.rpicam.config.OCVLocalCameraConfig;
import com.rpicam.config.VlcjCameraConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CameraServiceImpl implements CameraService {
    private static CameraServiceImpl instance;

    private ConfigService configService;
    
    private HashMap<UUID, CameraWorker> cameras = new HashMap<>();

    private CameraServiceImpl() {
        configService = ServiceLoader.load(ConfigService.class).findFirst().get();
        var configRoot = configService.getConfig();

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

    public static CameraServiceImpl provider() {
        if (instance == null) {
            instance = new CameraServiceImpl();
        }
        return instance;
    }

    @Override
    public void shutdown() {
        stopCameras();

        ArrayList<CameraConfig> cameraConfs = new ArrayList<>();
        for (var entry : cameras.entrySet()) {
            var cameraUUID = entry.getKey();
            var camera = entry.getValue();

            var conf = camera.toConfig();
            conf.uuid = cameraUUID.toString();
            cameraConfs.add(conf);
        }

        var configRoot = configService.getConfig();
        configRoot.cameras = new CameraConfig[cameraConfs.size()];
        cameraConfs.toArray(configRoot.cameras);
    }

    @Override
    public void startCameras() {
        for (var c : cameras.values()) {
            try {
                c.start();
            } catch (Throwable t) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Camera failed to start", t);
            }
        }
    }

    @Override
    public void stopCameras() {
        for (var c : cameras.values()) {
            try {
                c.stop();
            } catch (Throwable t) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Camera failed to stop", t);
            }
        }
    }

    @Override
    public UUID addCamera(CameraWorker camera) {
        var cameraUUID = UUID.randomUUID();
        addCamera(camera, cameraUUID);
        return cameraUUID;
    }

    @Override
    public void addCamera(CameraWorker camera, UUID cameraUUID) {
        cameras.put(cameraUUID, camera);
    }

    @Override
    public void removeCamera(UUID cameraUUID) {
        CameraWorker camera = cameras.get(cameraUUID);
        camera.stop();
        cameras.remove(cameraUUID);
    }

    @Override
    public CameraWorker getCamera(UUID cameraUUID) {
        return cameras.get(cameraUUID);
    }
}
