module com.rpicam {
    requires java.desktop;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.bytedeco.opencv;
    requires com.google.gson;
    requires org.controlsfx.controls;
    requires uk.co.caprica.vlcj;
    requires org.apache.commons.lang3;

    provides com.rpicam.config.ConfigService with com.rpicam.config.ConfigServiceImpl;
    provides com.rpicam.detection.ClassifierService with com.rpicam.detection.SingleThreadClassifierService;
    provides com.rpicam.cameras.CameraService with com.rpicam.cameras.CameraServiceImpl;
    provides com.rpicam.scenes.SceneService with com.rpicam.scenes.SceneServiceImpl;
    uses com.rpicam.config.ConfigService;
    uses com.rpicam.detection.ClassifierService;
    uses com.rpicam.cameras.CameraService;
    uses com.rpicam.scenes.SceneService;

    opens com.rpicam.javafx.views to javafx.fxml;
    exports com.rpicam.javafx to javafx.graphics;
    opens com.rpicam.config to com.google.gson;
}
