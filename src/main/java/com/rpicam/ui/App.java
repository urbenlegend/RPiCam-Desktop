package com.rpicam.ui;

import com.rpicam.config.ConfigManager;
import com.rpicam.video.CameraManager;
import java.io.IOException;
import java.nio.file.Paths;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {
    private static final String CONFIG_PATH = "./data/config.json";

    private static ConfigManager configManager = new ConfigManager();
    private static CameraManager cameraManager = new CameraManager();

    public static CameraManager getCameraManager() {
        return cameraManager;
    }

    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        configManager.loadConfigFile(Paths.get(CONFIG_PATH));

        cameraManager.loadConfig();
        cameraManager.startCameras();

        var dashboard = new Dashboard();

        var scene = new Scene(dashboard);
        stage.setScene(scene);

        stage.setTitle("RPiCam");
        stage.show();
    }

    @Override
    public void stop() throws IOException {
        cameraManager.stopCameras();
        cameraManager.saveConfig();
        configManager.saveConfigFile(Paths.get(CONFIG_PATH));
    }
}
