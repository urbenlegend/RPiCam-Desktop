package com.rpicam.video;

import com.rpicam.config.CameraConfig;
import com.rpicam.models.CameraModel;

public interface CameraWorker {

    void start();

    void stop();

    CameraModel getModel();

    CameraConfig getConfig();
}
