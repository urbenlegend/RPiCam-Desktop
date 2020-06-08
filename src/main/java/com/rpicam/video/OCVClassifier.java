/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.video;

import java.util.function.Consumer;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

/**
 *
 * @author benrx
 */
public class OCVClassifier implements OCVFrameHandler {
    private CascadeClassifier classifier;
    private Scalar color;
    private Consumer<Rect[]> frameCallback;
    
    public OCVClassifier(String path) {
        classifier = new CascadeClassifier();
        classifier.load(path);
        color = new Scalar(0, 0, 0);
        frameCallback = array -> {};
    }
    
    public void setColor(int r, int g, int b) {
        r = Math.max(0, Math.min(r, 255));
        g = Math.max(0, Math.min(g, 255));
        b = Math.max(0, Math.min(b, 255));
        color = new Scalar(r, g, b);
    }
    
    public Scalar getColor() {
        return color;
    }
    
    public void handleFrame(Mat frame) {
        MatOfRect detectedObjs = new MatOfRect();
        int minSize = Math.round(frame.rows() * 0.1f);

        // TODO: Check if correct parameters are being used
        classifier.detectMultiScale(frame,
                detectedObjs,
                1.1,
                3,
                Objdetect.CASCADE_SCALE_IMAGE,
                new Size(minSize, minSize),
                new Size()
        );
        
        // TODO: Replace with draw code on UI side
        Rect[] objArray = detectedObjs.toArray();
        for (Rect obj : objArray) {
            Imgproc.rectangle(frame, obj.tl(), obj.br(), color, 3);
        }
        
        frameCallback.accept(objArray);
    }
    
    public void setOnHandleFrame(Consumer<Rect[]> callback) {
        frameCallback = callback;
    }
}
