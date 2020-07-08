package com.rpicam.video;

import com.rpicam.models.CameraViewModel;

public interface CameraWorker {

    void start();

    void stop();

    CameraViewModel getViewModel();

    String toJson();

    void fromJson(String jsonStr);
}
