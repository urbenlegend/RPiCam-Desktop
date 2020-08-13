package com.rpicam.cameras;

public class StatsResult {
    public final String cameraName;
    public final String cameraStatus;
    public final String fps;
    public final String resolution;
    public final String networkStatus;
    public final String timestamp;

    public StatsResult(String cameraName, String cameraStatus, String fps, String resolution, String networkStatus, String timestamp) {
        this.cameraName = cameraName;
        this.cameraStatus = cameraStatus;
        this.fps = fps;
        this.resolution = resolution;
        this.networkStatus = networkStatus;
        this.timestamp = timestamp;
    }
}
