/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.video;

import com.rpicam.exceptions.VideoIOException;
import java.util.*;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

/**
 *
 * @author benrx
 */
public class OCVCamera {
    private VideoCapture capture;
    private ArrayList<OCVFrameHandler> handlers;

    public OCVCamera() {
        // Initialize camera
        capture = new VideoCapture();
        handlers = new ArrayList<>();
    }
    
    public void addFrameHandler(OCVFrameHandler h) {
        handlers.add(h);
    }
    
    public void removeFrameHandler(OCVFrameHandler h) {
        handlers.remove(h);
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

    public Mat getFrame() {
        Mat frame = getRawFrame();
                 
        for (var c : handlers) {
            c.handleFrame(frame);
        }
        
        return frame;
    }
    
    public Mat getRawFrame() {
        Mat frame = new Mat();
        if (!capture.read(frame)) {
            throw new VideoIOException("could not grab next frame from camera");
        }
        return frame;
    }
}