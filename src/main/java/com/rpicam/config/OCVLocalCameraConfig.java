package com.rpicam.config;

public class OCVLocalCameraConfig extends OCVCameraConfig {
    public int camIndex;
    public String captureApi;
    public int widthRes;
    public int heightRes;
    public int capRate;
    public int procRate;

    public OCVLocalCameraConfig() {
        type = "local";
    }
}
