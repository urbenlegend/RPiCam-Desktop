package com.rpicam.config;

public class VlcjCameraConfig extends OCVCameraConfig {
    public String url;
    public int procInterval;

    public VlcjCameraConfig() {
        type = "path";
    }
}
