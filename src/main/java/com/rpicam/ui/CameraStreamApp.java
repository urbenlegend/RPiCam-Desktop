/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.ui;

import com.rpicam.video.OCVCamera;
import com.rpicam.video.OCVClassifier;
import com.rpicam.video.VideoUtils;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

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

        // Register classifiers as camera frame handlers
        var upperBodyModel = new OCVClassifier("./data/upperbody_recognition_model.xml");
        var facialModel = new OCVClassifier("./data/facial_recognition_model.xml");
        var fullBodyModel = new OCVClassifier("./data/fullbody_recognition_model.xml");
        upperBodyModel.setColor(255, 0, 0);
        facialModel.setColor(0, 255, 0);
        fullBodyModel.setColor(0, 0, 255);
        camera.addFrameHandler(upperBodyModel);
        camera.addFrameHandler(facialModel);
        camera.addFrameHandler(fullBodyModel);
        
        camera.open(0);
        
        // Capture a test frame to get video dimensions later
        Image testImg = VideoUtils.toJFXImage(camera.getFrame());

        // Create JavaFX window
        ImageView imageView = new ImageView();
        HBox hbox = new HBox(imageView);
        Scene scene = new Scene(hbox, testImg.getWidth(), testImg.getHeight());
        stage.setScene(scene);
        stage.setTitle("Camera Stream");
        stage.show();

        // Capture loop
        cameraTimer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                imageView.setImage(VideoUtils.toJFXImage(camera.getFrame()));
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