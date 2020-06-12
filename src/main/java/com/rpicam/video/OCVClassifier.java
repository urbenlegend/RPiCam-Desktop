/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.video;

import java.util.ArrayList;
import java.util.function.Function;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

/**
 *
 * @author benrx
 */
public class OCVClassifier implements Function<Mat, ArrayList<ClassifierResult>> {
    private CascadeClassifier classifier;
    private String title = "";
    private int r = 255, g = 255, b = 255;
    
    public OCVClassifier(String path) {
        classifier = new CascadeClassifier();
        classifier.load(path);
    }
    
    public void setTitle(String name) {
        title = name;
    }
    
    public void setRGB(int red, int green, int blue) {
        r = red;
        g = green;
        b = blue;
    }
    
    @Override
    public ArrayList<ClassifierResult> apply(Mat frame) {
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
        
        ArrayList<ClassifierResult> results = new ArrayList<>();
        for (var obj : detectedObjs.toArray()) {
            results.add(new ClassifierResult(obj.x, obj.y, obj.width, obj.height, title, r, g, b));
        }
        
        return results;
    }
}