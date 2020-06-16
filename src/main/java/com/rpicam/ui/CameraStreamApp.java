package com.rpicam.ui;

import com.rpicam.video.OCVClassifier;
import com.rpicam.video.OCVVideoCapture;
import com.rpicam.video.OCVVideoWorker;
import com.rpicam.video.VideoManager;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class CameraStreamApp extends Application {
    VideoManager videoManager;
    private OCVVideoCapture camera;
    

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage stage) throws IOException {
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
        var cameraWorker = new OCVVideoWorker(camera, cameraView.getCameraModel());
        cameraWorker.addClassifier(upperBodyModel);
        cameraWorker.addClassifier(facialModel);
        cameraWorker.addClassifier(fullBodyModel);

        videoManager = new VideoManager();
        videoManager.addWorker(cameraWorker, 16, 80);
        
        FXMLLoader dashboardLoader = new FXMLLoader(getClass().getResource("Dashboard.fxml"));
        Parent dashboard = dashboardLoader.load();
        DashboardController dashController = dashboardLoader.getController();
        dashController.getLayout().add(cameraView, 1, 1);
        
        var scene = new Scene(dashboard);
        stage.setScene(scene);
        stage.setTitle("RPiCam");
        stage.show();
    }
    
    @Override
    public void stop() {
        videoManager.stopWorkers();
        camera.release();
    }
}