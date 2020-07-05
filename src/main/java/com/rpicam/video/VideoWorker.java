package com.rpicam.video;

import com.rpicam.ui.VideoViewModel;

public interface VideoWorker {
    void start();

    void stop();

    void bindModel(VideoViewModel model);

    void unbindModel(VideoViewModel model);

    default String toJSON() {
        return "";
    }
}
