package com.rpicam.javafx;

import static com.rpicam.Constants.APP_NAME;
import com.rpicam.cameras.CameraService;
import com.rpicam.config.ConfigService;
import com.rpicam.javafx.views.Dashboard;
import com.rpicam.detection.ClassifierService;
import com.rpicam.scenes.SceneService;
import java.io.IOException;
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

    @Override
    public void start(Stage stage) throws IOException {
        var dashboard = new Dashboard();
        var scene = new Scene(dashboard);
        stage.setScene(scene);
        stage.setTitle(APP_NAME);
        stage.show();

        ServiceLoader.load(CameraService.class).findFirst().get().startCameras();
    }

    @Override
    public void stop() throws IOException {
        ServiceLoader.load(SceneService.class).findFirst().get().shutdown();
        ServiceLoader.load(CameraService.class).findFirst().get().shutdown();
        ServiceLoader.load(ClassifierService.class).findFirst().get().shutdown();
        ServiceLoader.load(ConfigService.class).findFirst().get().shutdown();
    }
}
