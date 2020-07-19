package com.rpicam.video;

import com.rpicam.config.OCVCameraConfig;
import com.rpicam.util.Listenable;

public abstract class CameraWorker extends Listenable<CameraListener> {

    public abstract void start();

    public abstract void stop();

    public abstract OCVCameraConfig toConfig();

    public abstract void fromConfig(OCVCameraConfig conf);
}
