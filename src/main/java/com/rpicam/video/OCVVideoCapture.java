/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.video;

import com.rpicam.exceptions.VideoIOException;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

/**
 *
 * @author benrx
 */
public class OCVVideoCapture {
    private VideoCapture capture;

    public OCVVideoCapture() {
        // Initialize camera
        capture = new VideoCapture();
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
    
    public void open(String filename) {
        if (!capture.open(filename)) {
            throw new VideoIOException("Could not open video file " + filename);
        }
    }
    
    public void release() {
        capture.release();
    }
    
    public Mat getFrame() {
        Mat frame = new Mat();
        if (!capture.read(frame)) {
            throw new VideoIOException("could not grab next frame from camera");
        }
        return frame;
    }
}