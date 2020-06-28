package com.rpicam.video;

import java.util.List;

public interface VideoWorker {    
    default void open(int camIndex, int width, int height) {
        throw new UnsupportedOperationException("open via camera index");
    }
    
    default void open(String path) {
        throw new UnsupportedOperationException("open via path");
    }
    
    void close();
    
    void start(int grabRate, int processRate);
    
    void stop();
    
    List<VideoViewModel> getModels();
    
    default String toJSON() {
        return "";
    }
}
