/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.ui;

import com.rpicam.video.OCVCamera;
import com.rpicam.video.OCVClassifier;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.opencv.core.Mat;

/**
 *
 * @author benrx
 */
public class CameraStreamApp extends Application {
    OCVCamera camera;
    AnimationTimer cameraTimer;

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage stage) {
        camera = new OCVCamera();
        camera.open(0);

        var upperBodyModel = new OCVClassifier("./data/upperbody_recognition_model.xml");
        var facialModel = new OCVClassifier("./data/facial_recognition_model.xml");
        var fullBodyModel = new OCVClassifier("./data/fullbody_recognition_model.xml");
        
        var cameraView = new CameraView();
        
        var scene = new Scene(cameraView, 640, 480);
        stage.setScene(scene);
        stage.setTitle("Camera Stream");
        stage.show();

        // Capture loop
        cameraTimer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                // TODO: Separate grabbing the frame from UI thread
                var cameraModel = cameraView.getCameraModel();
                Mat frame = camera.getFrame();
                cameraModel.setMat(frame);
                
                // Apply classifiers and submit to model
                cameraModel.clearClassifierResults();
                for (var rect : upperBodyModel.apply(frame)) {
                    cameraModel.addClassifierResult("Upper Body", Color.rgb(255, 0, 0), rect.x, rect.y, rect.width, rect.height);
                }
                for (var rect : facialModel.apply(frame)) {
                    cameraModel.addClassifierResult("Face", Color.rgb(0, 255, 0), rect.x, rect.y, rect.width, rect.height);
                }
                for (var rect : fullBodyModel.apply(frame)) {
                    cameraModel.addClassifierResult("Full Body", Color.rgb(0, 0, 255), rect.x, rect.y, rect.width, rect.height);
                }
            }
        };
        cameraTimer.start();
    }
    
    @Override
    public void stop() {
        cameraTimer.stop();
        camera.release();
    }
}