package com.rpicam;

import com.rpicam.javafx.App;
import org.bytedeco.opencv.global.opencv_core;

/**
 * The Main class is the starting point of RPiCam
 */
public class Main {

    /**
     * The main method here simply serves to enable OpenCL in the OpenCV
     * library. After that it will defer to {@link com.rpicam.javafx.App} to
     * initialize the GUI and the rest of the application.
     *
     * @param args program arguments passed on application startup
     */
    public static void main(String[] args) {
        // Setup OpenCL
        opencv_core.setUseOpenCL(true);

        App.main(args);
    }
}
