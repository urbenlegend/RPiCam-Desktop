package com.rpicam.cameras;

import com.rpicam.detection.ClassifierResult;
import com.rpicam.config.OCVCameraConfig;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public abstract class CameraWorker {
    private List<Function<ByteBufferImage, ArrayList<ClassifierResult>>> classifiers = Collections.synchronizedList(new ArrayList<>());
    private transient PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    // Observable Properties
    private ByteBufferImage frame;
    private ArrayList<ClassifierResult> classifierResults;
    private String cameraName;
    private String videoStatus;
    private String cameraStatus;
    private String timestamp;

    public abstract OCVCameraConfig toConfig();

    public abstract void fromConfig(OCVCameraConfig conf);

    public abstract void start();

    public abstract void stop();

    public void addClassifier(Function<ByteBufferImage, ArrayList<ClassifierResult>> c) {
        classifiers.add(c);
    }

    public void removeClassifier(Function<ByteBufferImage, ArrayList<ClassifierResult>> c) {
        classifiers.remove(c);
    }

    public List<Function<ByteBufferImage, ArrayList<ClassifierResult>>> getClassifiers() {
        return classifiers;
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    public PropertyChangeSupport getPropertyChangeSupport() {
        return pcs;
    }

    public ByteBufferImage getFrame() {
        return frame;
    }

    protected void setFrame(ByteBufferImage frame) {
        this.frame = frame;
        pcs.firePropertyChange("frame", null, this.frame);
    }

    public ArrayList<ClassifierResult> getClassifierResults() {
        return classifierResults;
    }

    protected void setClassifierResults(ArrayList<ClassifierResult> classifierResults) {
        this.classifierResults = classifierResults;
        pcs.firePropertyChange("classifierResults", null, this.classifierResults);
    }

    public String getCameraName() {
        return cameraName;
    }

    protected void setCameraName(String cameraName) {
        this.cameraName = cameraName;
        pcs.firePropertyChange("cameraName", null, this.cameraName);
    }

    public String getVideoStatus() {
        return videoStatus;
    }

    protected void setVideoStatus(String videoStatus) {
        this.videoStatus = videoStatus;
        pcs.firePropertyChange("videoStatus", null, this.videoStatus);
    }

    public String getCameraStatus() {
        return cameraStatus;
    }

    protected void setCameraStatus(String cameraStatus) {
        this.cameraStatus = cameraStatus;
        pcs.firePropertyChange("cameraStatus", null, this.cameraStatus);
    }

    public String getTimestamp() {
        return timestamp;
    }

    protected void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        pcs.firePropertyChange("timestamp", null, this.timestamp);
    }
}
