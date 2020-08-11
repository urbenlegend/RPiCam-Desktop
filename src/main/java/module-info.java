module com.rpicam {
    requires java.desktop;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.bytedeco.opencv;
    requires com.google.gson;
    requires org.controlsfx.controls;
    requires uk.co.caprica.vlcj;

    opens com.rpicam.javafx.views to javafx.fxml;
    exports com.rpicam.javafx to javafx.graphics;
    opens com.rpicam.config to com.google.gson;
}
