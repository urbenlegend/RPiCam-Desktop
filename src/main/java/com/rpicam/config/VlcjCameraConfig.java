package com.rpicam.config;

public class VlcjCameraConfig extends OCVCameraConfig {
    public String url;
    public int procRate;

    public VlcjCameraConfig() {
        type = "path";
    }
}
