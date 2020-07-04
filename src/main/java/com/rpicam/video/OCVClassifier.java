/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.video;

import java.util.ArrayList;
import java.util.function.Function;
import static org.bytedeco.opencv.global.opencv_objdetect.CASCADE_SCALE_IMAGE;
import org.bytedeco.opencv.opencv_core.UMat;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

/**
 *
 * @author benrx
 */
public class OCVClassifier implements Function<UMat, ArrayList<ClassifierResult>> {
    private CascadeClassifier classifier = new CascadeClassifier();
    private String title = "";
    private String color = "";

    public OCVClassifier(String path) {
        classifier.load(path);
    }

    public void setTitle(String name) {
        title = name;
    }

    public void setRGB(String colorStr) {
        color = colorStr;
    }

    @Override
    public ArrayList<ClassifierResult> apply(UMat frame) {
        var detectedObjs = new RectVector();
        int minSize = Math.round(frame.rows() * 0.1f);

        // TODO: Check if correct parameters are being used
        classifier.detectMultiScale(frame,
                detectedObjs,
                1.1,
                3,
                CASCADE_SCALE_IMAGE,
                new Size(minSize, minSize),
                new Size()
        );

        var results = new ArrayList<ClassifierResult>();
        for (var obj : detectedObjs.get()) {
            results.add(new ClassifierResult(obj.x(), obj.y(), obj.width(), obj.height(), title, color));
        }

        return results;
    }
}