/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.video;

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
public class OpenCVDetection {
    private CascadeClassifier cascadeClassifier;
    
    public OpenCVDetection() {
        cascadeClassifier = new CascadeClassifier();
        cascadeClassifier.load("./src/main/resources/com/rpicam/video/haarcascade_frontalface_alt.xml");
    }
    
    public Mat detectFace(Mat inputImage) {
        MatOfRect facesDetected = new MatOfRect();
        int minFaceSize = Math.round(inputImage.rows() * 0.1f);
        
        cascadeClassifier.detectMultiScale(inputImage,
                facesDetected,
                1.1,
                3,
                Objdetect.CASCADE_SCALE_IMAGE,
                new Size(minFaceSize, minFaceSize),
                new Size()
        );
        Rect[] facesArray = facesDetected.toArray();
        for (Rect face : facesArray) {
            Imgproc.rectangle(inputImage, face.tl(), face.br(), new Scalar(0, 0, 255), 3);
        }
        return inputImage;
    }
}
