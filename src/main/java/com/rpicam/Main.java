package com.rpicam;

import com.rpicam.javafx.App;
import org.bytedeco.opencv.global.opencv_core;

public class Main {

    public static void main(String[] args) {
        // Setup OpenCL
        opencv_core.setUseOpenCL(true);

        App.main(args);
    }
}
