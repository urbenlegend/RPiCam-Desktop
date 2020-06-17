package com.rpicam.video;

public interface VideoWorker {    
    default void open(int camIndex) {
        throw new UnsupportedOperationException("open via camera index");
    }
    
    default void open(String path) {
        throw new UnsupportedOperationException("open via path");
    }
    
    void close();
    
    void start(int grabRate, int processRate);
    
    void stop();
    
    void bind(VideoViewModel model);
    
    void unbind();
    
    default String toJSON() {
        return "";
    }
}
