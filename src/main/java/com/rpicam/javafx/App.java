package com.rpicam.javafx;

import com.rpicam.cameras.CameraService;
import com.rpicam.config.ConfigService;
import com.rpicam.javafx.views.Dashboard;
import com.rpicam.detection.ClassifierService;
import com.rpicam.scenes.SceneService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ServiceLoader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * App is the JavaFX {@link javafx.application.Application} instance and serves
 * as the main "hub" for the rest of the program. It creates and initializes the
 * backend code
 * ({@link com.rpicam.config.ConfigServiceImpl}, {@link com.rpicam.cameras.CameraServiceImpl},
 * and {@link com.rpicam.scenes.SceneServiceImpl}), the JavaFX window
 * (javafx.stage.Stage and javafx.scene.Scene), and the frontend UI
 * ({@link com.rpicam.javafx.views.Dashboard})
 */
public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    private ConfigService configService;
    private ClassifierService classifierService;
    private CameraService cameraService;
    private SceneService sceneService;

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

        configService = ServiceLoader.load(ConfigService.class).findFirst().get();
        if (configPath.toFile().exists()) {
            configService.loadConfigFile(configPath);
        }
        else {
            configService.loadConfigFile(defaultsPath);
        }

        classifierService = ServiceLoader.load(ClassifierService.class).findFirst().get();
        cameraService = ServiceLoader.load(CameraService.class).findFirst().get();
        sceneService = ServiceLoader.load(SceneService.class).findFirst().get();

        cameraService.startCameras();

        var dashboard = new Dashboard();

        var scene = new Scene(dashboard);
        stage.setScene(scene);

        stage.setTitle(appName);
        stage.show();
    }

    @Override
    public void stop() throws IOException {
        sceneService.shutdown();
        cameraService.shutdown();
        classifierService.shutdown();

        // Before saving, make application config directory if it doesn't exist
        File configDir = configPath.getParent().toFile();
        if (!configDir.exists()) {
            configDir.mkdir();
        }
        configService.saveConfigFile(configPath);
    }
}
