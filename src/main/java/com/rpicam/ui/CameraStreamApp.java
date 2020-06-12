/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.ui;

import com.rpicam.video.VideoWorker;
import com.rpicam.video.OCVClassifier;
import com.rpicam.video.OCVVideoCapture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author benrx
 */
public class CameraStreamApp extends Application {
    private ScheduledExecutorService schedulePool;
    private AnimationTimer drawTimer;
    private OCVVideoCapture camera;
    

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage stage) {
        schedulePool = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        
        camera = new OCVVideoCapture();
        camera.open(0);
        
        var upperBodyModel = new OCVClassifier("./data/upperbody_recognition_model.xml");
        upperBodyModel.setTitle("Upper Body");
        upperBodyModel.setRGB(255, 0, 0);
        var facialModel = new OCVClassifier("./data/facial_recognition_model.xml");
        facialModel.setTitle("Face");
        facialModel.setRGB(0, 255, 0);
        var fullBodyModel = new OCVClassifier("./data/fullbody_recognition_model.xml");
        fullBodyModel.setTitle("Full Body");
        fullBodyModel.setRGB(0, 0, 255);
        
        var cameraView = new VideoView();
        var cameraWorker = new VideoWorker(camera, cameraView.getCameraModel());
        cameraWorker.addClassifier(upperBodyModel);
        cameraWorker.addClassifier(facialModel);
        cameraWorker.addClassifier(fullBodyModel);

        // Capture loop
        schedulePool.scheduleWithFixedDelay(cameraWorker::processFrame, 0, 16, TimeUnit.MILLISECONDS);
        
        // Draw loop
        drawTimer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                cameraWorker.sendToModel();
            }
        };
        
        var scene = new Scene(cameraView, 640, 480);
        stage.setScene(scene);
        stage.setTitle("Camera Stream");
        stage.show();
                
        drawTimer.start();
    }
    
    @Override
    public void stop() {
        schedulePool.shutdownNow();
        drawTimer.stop();
        camera.release();
    }
}