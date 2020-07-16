module com.rpicam {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.bytedeco.opencv;
    requires com.google.gson;
    requires org.controlsfx.controls;

    opens com.rpicam.ui to javafx.fxml;
    exports com.rpicam.ui to javafx.graphics;
    opens com.rpicam.video to com.google.gson;
    opens com.rpicam.config to com.google.gson;
}
