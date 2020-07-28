package com.rpicam.config;

public class OCVStreamCameraConfig extends OCVCameraConfig {
    public String url;
    public String captureApi;
    public int capRate;
    public int procRate;

    public OCVStreamCameraConfig() {
        type = "path";
    }
}
