module com.rpicam {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.bytedeco.opencv;
    requires com.google.gson;
    requires org.controlsfx.controls;

    opens com.rpicam.javafx to javafx.fxml;
    exports com.rpicam.javafx to javafx.graphics;
    opens com.rpicam.config to com.google.gson;
}
