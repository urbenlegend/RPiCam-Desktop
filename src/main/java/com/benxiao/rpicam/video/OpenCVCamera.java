/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.benxiao.rpicam.video;

import com.benxiao.rpicam.exceptions.VideoIOException;

import javafx.scene.image.Image;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

/**
 *
 * @author benrx
 */
public class OpenCVCamera {
    private VideoCapture capture;
    private OpenCVDetection detection;

    public OpenCVCamera() {
        // Initialize camera
        capture = new VideoCapture();
        detection = new OpenCVDetection();
    }
    
    public void open(int camIndex) {
        // Detect OS and use the right camera API
        // Necessary because CAP_ANY is too slow, but it is used for fallback
        String os = System.getProperty("os.name").toLowerCase();
        int videoAPI = Videoio.CAP_ANY;
        if (os.contains("win")) {
            videoAPI = Videoio.CAP_DSHOW;
        }
        else if (os.contains("mac")) {
            videoAPI = Videoio.CAP_AVFOUNDATION;
        }
        else if (os.contains("linux")) {
            videoAPI = Videoio.CAP_V4L2;
        }
        
        if (!capture.open(camIndex, videoAPI)) {
            throw new VideoIOException("Could not open camera " + camIndex);
        }
    }
    
    public void release() {
        capture.release();
    }

    public Image getImage(boolean detect) {
        Mat mat = getRawFrame();
        
        if (detect) {
            return VideoUtils.mat2Img(detection.detectFace(mat));
        }
        else {
            return VideoUtils.mat2Img(mat);
        }
    }
    
    public Mat getRawFrame() {
        Mat mat = new Mat();
        if (!capture.read(mat)) {
            throw new VideoIOException("could not grab next frame from camera");
        }
        return mat;
    }
}