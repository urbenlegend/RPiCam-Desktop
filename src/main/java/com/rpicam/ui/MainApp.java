package com.rpicam.ui;

import com.rpicam.video.VideoManager;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static VideoManager videoManager = new VideoManager();

    public static VideoManager getVideoManager() {
        return videoManager;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        videoManager.loadSources("./data/config.json");
        videoManager.startWorkers();

        Parent dashboard = FXMLLoader.load(getClass().getResource("Dashboard.fxml"));
        var scene = new Scene(dashboard);
        stage.setScene(scene);

        stage.setTitle("RPiCam");
        stage.show();
    }

    @Override
    public void stop() {
        videoManager.stopWorkers();
    }

}
