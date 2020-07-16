package com.rpicam.video;

import com.rpicam.config.OCVCameraConfig;
import com.rpicam.util.Listenable;

public interface CameraWorker extends Listenable<CameraListener> {

    void start();

    void stop();

    OCVCameraConfig toConfig();

    void fromConfig(OCVCameraConfig conf);
}
