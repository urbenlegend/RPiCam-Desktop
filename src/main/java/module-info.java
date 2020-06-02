module com.benxiao.rpicam {
    requires javafx.controls;
    requires javafx.fxml;
    requires opencv;

    opens com.benxiao.rpicam.ui to javafx.fxml;
    exports com.benxiao.rpicam.ui to javafx.graphics;
}
