package com.rpicam.video;

import com.rpicam.util.Listenable;

public interface CameraWorker extends Listenable<CameraListener> {

    void start();

    void stop();

    String toJson();

    void fromJson(String jsonStr);
}
