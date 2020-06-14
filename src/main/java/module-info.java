module com.rpicam {
    requires javafx.controls;
    requires javafx.fxml;
    requires opencv;
    requires java.base;

    opens com.rpicam.ui to javafx.fxml;
    exports com.rpicam.ui to javafx.graphics;
}
