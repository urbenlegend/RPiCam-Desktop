package com.rpicam.video;


public interface VideoWorker {
    public void grabFrame();
    public void processFrame();
    public void updateUI();
}
