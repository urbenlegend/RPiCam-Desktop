package com.rpicam.detection;

import com.rpicam.cameras.ByteBufferImage;
import java.util.List;

public interface ClassifierService {
    public void addClassifier(Classifier c);
    public void removeClassifier(Classifier c);
    public void shutdown();
    public List<ClassifierResult> submit(ByteBufferImage image) throws InterruptedException;
}
