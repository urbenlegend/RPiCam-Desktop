/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.video;

import com.rpicam.exceptions.VideoIOException;
import java.util.*;
import java.util.concurrent.*;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

/**
 *
 * @author benrx
 */
public class OCVCamera {
    private VideoCapture capture;
    private ExecutorService executor;
    private ArrayList<OCVClassifier> classifiers;

    public OCVCamera() {
        // Initialize camera
        capture = new VideoCapture();
        executor = Executors.newCachedThreadPool();
        classifiers = new ArrayList<>();
    }
    
    public void addClassifier(String path) {
        classifiers.add(new OCVClassifier(path));
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
        executor.shutdown();
    }

    public Mat getFrame(boolean detect) {
        Mat frame = getRawFrame();
        
        if (detect) {            
            for (var c : classifiers) {
                Rect[] objArray = c.process(frame);
                for (Rect obj : objArray) {
                    Imgproc.rectangle(frame, obj.tl(), obj.br(), new Scalar(0, 0, 255), 3);
                }
            }
        }
        
        return frame;
    }
    
    public Mat getFrameMultithreaded(boolean detect) {
        Mat frame = getRawFrame();
        
        if (detect) {
            ArrayList<Future<Rect[]>> classResults = new ArrayList<>();
            try {
                for (var c : classifiers) {
                    classResults.add(executor.submit(() -> {
                        return c.process(frame);
                    }));
                }
                for (var r : classResults) {
                    Rect[] objArray = r.get();
                    for (Rect obj : objArray) {
                        Imgproc.rectangle(frame, obj.tl(), obj.br(), new Scalar(0, 0, 255), 3);
                    }
                }
            }
            catch (InterruptedException e) {
                throw new VideoIOException("Classifier threads interrupted", e);
            }
            catch (ExecutionException e) {
                throw new VideoIOException("Classifier thread error", e);
            }
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