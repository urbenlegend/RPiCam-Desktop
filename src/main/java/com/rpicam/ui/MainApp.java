package com.rpicam.ui;

import com.rpicam.video.VideoManager;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class MainApp extends Application {
    private VideoManager videoManager = VideoManager.getInstance();
    
    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void start(Stage stage) throws IOException {        
        videoManager.loadSources("./data/config.json");
        
        Parent dashboard = FXMLLoader.load(getClass().getResource("Dashboard.fxml"));
        var scene = new Scene(dashboard);
        stage.setScene(scene);
        
        // Make window decoration-less
        stage.initStyle(StageStyle.TRANSPARENT);
        ResizeHelper.addResizeListener(stage);
        
        stage.setTitle("RPiCam");
        stage.show();
    }
    
    @Override
    public void stop() {
        videoManager.stopWorkers();
    }
}