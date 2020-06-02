/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.benxiao.rpicam.ui;

import com.benxiao.rpicam.video.OpenCVCamera;

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
    OpenCVCamera camera;
    AnimationTimer cameraTimer;

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage stage) {
        camera = new OpenCVCamera();
        camera.open(0);
        
        // Capture a test frame to get video dimensions later
        Image testImg = camera.getImage(false);

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
                imageView.setImage(camera.getImage(true));
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