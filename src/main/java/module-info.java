module com.rpicam {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.bytedeco.opencv;
    requires java.base;
    requires org.json;

    opens com.rpicam.ui to javafx.fxml;
    exports com.rpicam.ui to javafx.graphics;
}
