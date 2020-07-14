package com.rpicam.video;

import com.rpicam.dto.video.ClassifierResult;
import java.util.List;
import org.bytedeco.opencv.opencv_core.UMat;

public interface CameraListener {

    void onClassifierResults(List<ClassifierResult> results);

    void onFrame(UMat mat);
}
