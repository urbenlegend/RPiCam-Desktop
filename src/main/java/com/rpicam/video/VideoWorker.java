package com.rpicam.video;

import com.rpicam.ui.VideoViewModel;
import java.util.List;

public interface VideoWorker {
    void start();

    void stop();

    List<VideoViewModel> getModels();

    default String toJSON() {
        return "";
    }
}
