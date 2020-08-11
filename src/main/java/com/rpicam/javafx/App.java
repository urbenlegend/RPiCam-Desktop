package com.rpicam.javafx;

import com.rpicam.javafx.views.Dashboard;
import com.rpicam.config.ConfigManager;
import com.rpicam.cameras.CameraManager;
import com.rpicam.scenes.SceneManager;
import java.io.IOException;
import java.nio.file.Paths;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * App is the JavaFX {@link javafx.application.Application} instance and serves
 * as the main "hub" for the rest of the program. It creates and initializes the
 * backend code
 * ({@link com.rpicam.config.ConfigManager}, {@link com.rpicam.cameras.CameraManager},
 * and {@link com.rpicam.scenes.SceneManager}), the JavaFX window
 * (javafx.stage.Stage and javafx.scene.Scene), and the frontend UI
 * ({@link com.rpicam.javafx.Dashboard})
 */
public class App extends Application {
    private static final String CONFIG_PATH = "./data/config.json";

    private static ConfigManager configManager = new ConfigManager();
    private static CameraManager cameraManager = new CameraManager();
    private static SceneManager sceneManager = new SceneManager();

    public static CameraManager cameraManager() {
        return cameraManager;
    }

    public static ConfigManager configManager() {
        return configManager;
    }

    public static SceneManager sceneManager() {
        return sceneManager;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        configManager.loadConfigFile(Paths.get(CONFIG_PATH));

        cameraManager.loadConfig();
        sceneManager.loadConfig();
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
        sceneManager.saveConfig();
        cameraManager.saveConfig();
        configManager.saveConfigFile(Paths.get(CONFIG_PATH));
    }
}
