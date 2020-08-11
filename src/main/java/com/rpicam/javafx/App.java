package com.rpicam.javafx;

import com.rpicam.javafx.views.Dashboard;
import com.rpicam.config.ConfigManager;
import com.rpicam.cameras.CameraManager;
import com.rpicam.scenes.SceneManager;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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
 * ({@link com.rpicam.javafx.views.Dashboard})
 */
public class App extends Application {
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

    private Path configPath;
    private Path defaultsPath = Paths.get("data/defaults.json");

    @Override
    public void start(Stage stage) throws IOException {
        String appName = "RPiCam";
        String configFileName = "config.json";

        if (System.getProperty("os.name").startsWith("Windows")) {
            configPath = Paths.get(String.format("%s\\%s\\%s", System.getenv("APPDATA"), appName, configFileName));
        }
        else {
            configPath = Paths.get(String.format("%s/.%s/%s", System.getProperty("user.home"), appName.toLowerCase(), configFileName));
        }

        if (configPath.toFile().exists()) {
            configManager.loadConfigFile(configPath);
        }
        else {
            configManager.loadConfigFile(defaultsPath);
        }

        cameraManager.loadConfig();
        sceneManager.loadConfig();
        cameraManager.startCameras();

        var dashboard = new Dashboard();

        var scene = new Scene(dashboard);
        stage.setScene(scene);

        stage.setTitle(appName);
        stage.show();
    }

    @Override
    public void stop() throws IOException {
        cameraManager.stopCameras();
        sceneManager.saveConfig();
        cameraManager.saveConfig();

        // Before saving, make application config directory if it doesn't exist
        File configDir = configPath.getParent().toFile();
        if (!configDir.exists()) {
            configDir.mkdir();
        }
        configManager.saveConfigFile(configPath);
    }
}
