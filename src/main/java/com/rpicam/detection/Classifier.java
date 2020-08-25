package com.rpicam.detection;

import com.rpicam.cameras.ByteBufferImage;
import com.rpicam.config.ClassifierConfig;
import java.util.List;
import java.util.function.Function;

public interface Classifier extends Function<ByteBufferImage, List<ClassifierResult>> {
    List<ClassifierResult> apply(ByteBufferImage image);
    ClassifierConfig toConfig();
}
