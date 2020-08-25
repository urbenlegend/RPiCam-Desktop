package com.rpicam.cameras;

import java.util.UUID;

public interface CameraService {
    void shutdown();
    void startCameras();
    void stopCameras();
    UUID addCamera(CameraWorker camera);
    void addCamera(CameraWorker camera, UUID cameraUUID);
    void removeCamera(UUID cameraUUID);
    CameraWorker getCamera(UUID cameraUUID);
}
