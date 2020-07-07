package com.rpicam.video;

import com.rpicam.ui.VideoModel;

public interface VideoWorker {

    void start();

    void stop();

    VideoModel getModel();

    default String toJSON() {
        return "";
    }
}
