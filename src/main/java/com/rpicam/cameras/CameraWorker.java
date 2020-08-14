package com.rpicam.cameras;

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
}
