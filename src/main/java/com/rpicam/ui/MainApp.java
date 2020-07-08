package com.rpicam.ui;

import com.rpicam.video.CameraManager;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    private static final String CONFIG_PATH = "./data/config.json";

    private CameraManager cameraManager = new CameraManager();

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        cameraManager.loadConfig(CONFIG_PATH);
        cameraManager.startWorkers();

        FXMLLoader dashboardLoader = new FXMLLoader(getClass().getResource("Dashboard.fxml"));
        Parent dashboard = dashboardLoader.load();
        DashboardController dashboardController = dashboardLoader.getController();
        dashboardController.setApp(this);

        var scene = new Scene(dashboard);
        stage.setScene(scene);

        stage.setTitle("RPiCam");
        stage.show();
    }

    @Override
    public void stop() throws IOException {
        cameraManager.stopWorkers();
        cameraManager.saveConfig(CONFIG_PATH);
    }
}
