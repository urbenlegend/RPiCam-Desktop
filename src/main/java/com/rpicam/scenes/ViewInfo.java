package com.rpicam.scenes;

import com.rpicam.config.ViewConfig;
import java.util.UUID;

public class ViewInfo {
    public UUID cameraUUID;
    public boolean drawStats;
    public boolean drawDetection;
    public int x;
    public int y;
    public int w;
    public int h;

    public void fromConfig(ViewConfig config) {
        cameraUUID = UUID.fromString(config.cameraUUID);
        drawStats = config.drawStats;
        drawDetection = config.drawDetection;
        x = config.x;
        y = config.y;
        w = config.w;
        h = config.h;
    }

    public ViewConfig toConfig() {
        var config = new ViewConfig();
        config.cameraUUID = cameraUUID.toString();
        config.drawStats = drawStats;
        config.drawDetection = drawDetection;
        config.x = x;
        config.y = y;
        config.w = w;
        config.h = h;
        return config;
    }
}
