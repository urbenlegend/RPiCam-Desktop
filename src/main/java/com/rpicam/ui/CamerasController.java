package com.rpicam.ui;

import com.rpicam.video.VideoManager;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import org.controlsfx.control.PopOver;

/**
 * FXML Controller class
 *
 * @author benrx
 */
public class CamerasController implements Initializable {
    @FXML
    private BorderPane mainLayout;
    @FXML
    private FlowPane cameraList;
    @FXML
    private Slider zoomSlider;
    @FXML
    private Button addCameraBtn;

    private Parent addCameraSettings;
    PopOver addCameraPopOver;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            addCameraSettings = FXMLLoader.load(getClass().getResource("CameraSettings.fxml"));
            addCameraPopOver = new PopOver(addCameraSettings);
            addCameraPopOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_LEFT);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        var videoManager = VideoManager.getInstance();
        for (var worker : videoManager.getWorkers().values()) {
            var cameraView = new VideoView();
            cameraView.prefWidthProperty().bind(zoomSlider.valueProperty());
            cameraView.prefHeightProperty().bind(zoomSlider.valueProperty()
                    .multiply(cameraView.frameHeightProperty())
                    .divide(cameraView.frameWidthProperty()));
            worker.getModels().add(cameraView.getCameraModel());
            cameraList.getChildren().add(cameraView);
        }
    }

    @FXML
    public void addCameraClicked() {
        addCameraPopOver.show(addCameraBtn);
    }
}
