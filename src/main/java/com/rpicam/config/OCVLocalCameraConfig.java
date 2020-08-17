package com.rpicam.config;

public class OCVLocalCameraConfig extends OCVCameraConfig {
    public int camIndex;
    public String captureApi;
    public int widthRes;
    public int heightRes;
    public int capRate;
    public int procInterval;

    public OCVLocalCameraConfig() {
        type = "local";
    }
}
