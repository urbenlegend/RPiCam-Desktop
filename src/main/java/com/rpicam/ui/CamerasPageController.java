package com.rpicam.ui;

import com.rpicam.models.CameraModel;
import com.rpicam.models.CameraManagerModel;
import com.rpicam.exceptions.UIException;
import java.io.IOException;
import javafx.beans.property.SimpleListProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.FlowPane;
import org.controlsfx.control.PopOver;

/**
 * FXML Controller class
 *
 * @author benrx
 */
public class CamerasPageController {

    @FXML
    private Button addCameraBtn;
    @FXML
    private FlowPane cameraFlowPane;
    @FXML
    private ScrollPane cameraScrollPane;
    @FXML
    private Slider zoomSlider;
    private PopOver addCameraPopOver;
    private Parent addCameraSettings;
    private FXMLLoader cameraSettingsLoader = new FXMLLoader(getClass().getResource("CameraSettings.fxml"));

    private CameraManagerModel videoListModel;
    private SimpleListProperty<CameraModel> selection = new SimpleListProperty<>();
    private SimpleListProperty<CameraModel> cameraList = new SimpleListProperty<>();

    @FXML
    public void initialize() {
        try {
            addCameraSettings = cameraSettingsLoader.load();
            addCameraPopOver = new PopOver(addCameraSettings);
            addCameraPopOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_LEFT);
        } catch (IOException ex) {
            throw new UIException("CamerasController failed to load camera settings pop over", ex);
        }

        cameraList.addListener((obs, oldVal, newVal) -> {
            cameraFlowPane.getChildren().clear();
            for (var model : newVal) {
                var cameraView = new CameraView();
                cameraView.prefWidthProperty().bind(zoomSlider.valueProperty()
                        .multiply(cameraScrollPane.widthProperty().subtract(2)));
                cameraView.prefHeightProperty().bind(zoomSlider.valueProperty()
                        .multiply(cameraScrollPane.widthProperty().subtract(2))
                        .multiply(cameraView.frameHeightProperty())
                        .divide(cameraView.frameWidthProperty()));
                cameraView.setModel(model);
                cameraFlowPane.getChildren().add(cameraView);
            }
        });

        var addCameraPopOverResults = cameraSettingsLoader.<CameraSettingsController>getController().resultsProperty();
        addCameraPopOverResults.addListener((obs, oldVal, newVal) -> {
            videoListModel.addCamera(newVal);
            addCameraPopOver.hide();
        });
    }

    public void setModel(CameraManagerModel aVideoListModel) {
        videoListModel = aVideoListModel;
        cameraList.bind(videoListModel.cameraListProperty());
        selection.bind(videoListModel.selectionProperty());
    }

    @FXML
    public void addCameraClicked() {
        addCameraPopOver.show(addCameraBtn);
    }
}
