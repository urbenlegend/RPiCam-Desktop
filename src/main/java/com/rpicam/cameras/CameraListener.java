package com.rpicam.cameras;

import com.rpicam.detection.ClassifierResult;
import java.nio.ByteBuffer;
import java.util.List;
import org.bytedeco.opencv.opencv_core.UMat;

public interface CameraListener {

    void onClassifierResults(List<ClassifierResult> results);

    default void onFrame(UMat mat) {
        throw new UnsupportedOperationException("UMats are not supported");
    }

    default void onFrame(ByteBuffer buffer, int width, int height) {
        throw new UnsupportedOperationException("JavaFX images are not supported");
    }
}
