/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.video;

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
public class OCVClassifier {
    private CascadeClassifier classifier;
    
    public OCVClassifier(String path) {
        classifier = new CascadeClassifier();
        classifier.load(path);
    }
    
    public Rect[] process(Mat inputImage) {
        MatOfRect detectedObjs = new MatOfRect();
        int minSize = Math.round(inputImage.rows() * 0.1f);

        classifier.detectMultiScale(inputImage,
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
