/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.ui;

import com.rpicam.video.OCVCamera;
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
        //camera.addClassifier("./src/main/resources/com/rpicam/video/haarcascade_frontalface_alt.xml");
        camera.addClassifier("./src/main/resources/com/rpicam/video/upperbody_recognition_model.xml");
        camera.addClassifier("./src/main/resources/com/rpicam/video/facial_recognition_model.xml");
        camera.addClassifier("./src/main/resources/com/rpicam/video/fullbody_recognition_model.xml");
        camera.open(0);
        
        // Capture a test frame to get video dimensions later
        Image testImg = VideoUtils.toJFXImage(camera.getFrame(false));

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
                imageView.setImage(VideoUtils.toJFXImage(camera.getFrameMultithreaded(true)));
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