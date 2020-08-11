package com.rpicam.cameras;

import com.rpicam.config.OCVCameraConfig;
import java.beans.PropertyChangeListener;

public interface CameraWorker {
    public abstract void start();
    public abstract void stop();
    public abstract OCVCameraConfig toConfig();
    public abstract void fromConfig(OCVCameraConfig conf);
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);
}
