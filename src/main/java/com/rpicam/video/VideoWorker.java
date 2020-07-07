package com.rpicam.video;

import com.rpicam.ui.VideoModel;

public interface VideoWorker {

    VideoModel getModel();

    void start();

    void stop();

    default String toJSON() {
        return "";
    }
}
