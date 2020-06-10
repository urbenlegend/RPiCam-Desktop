/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.video;

import java.util.function.Function;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

/**
 *
 * @author benrx
 */
public class OCVClassifier implements Function<Mat, Rect[]> {
    private CascadeClassifier classifier;
    
    public OCVClassifier(String path) {
        classifier = new CascadeClassifier();
        classifier.load(path);
    }
    
    @Override
    public Rect[] apply(Mat frame) {
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
        
        return detectedObjs.toArray();
    }
}