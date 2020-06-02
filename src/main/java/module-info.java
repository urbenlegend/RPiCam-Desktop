module com.rpicam {
    requires javafx.controls;
    requires javafx.fxml;
    requires opencv;

    opens com.rpicam.ui to javafx.fxml;
    exports com.rpicam.ui to javafx.graphics;
}
