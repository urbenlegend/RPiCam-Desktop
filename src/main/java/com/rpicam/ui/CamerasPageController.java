package com.rpicam.ui;

import com.rpicam.exceptions.UIException;
import java.io.IOException;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
    private PopOver addCameraPopOver;
    private Parent addCameraSettings;
    @FXML
    private FlowPane cameraList;
    @FXML
    private ScrollPane cameraPane;
    private SimpleListProperty<VideoModel> selection = new SimpleListProperty<>();
    private SimpleListProperty<VideoModel> videoList = new SimpleListProperty<>();
    private VideoListModel videoListModel;
    @FXML
    private Slider zoomSlider;

    @FXML
    public void addCameraClicked() {
        addCameraPopOver.show(addCameraBtn);
    }

    public void setModel(VideoListModel aVideoListModel) {
        videoListModel = aVideoListModel;
        videoList.bind(videoListModel.videoListProperty());
        selection.bind(videoListModel.selectionProperty());
    }

    @FXML
    public void initialize() {
        try {
            var cameraSettingsLoader = new FXMLLoader(getClass().getResource("CameraSettings.fxml"));
            addCameraSettings = cameraSettingsLoader.load();
            addCameraPopOver = new PopOver(addCameraSettings);
            addCameraPopOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_LEFT);
        } catch (IOException ex) {
            throw new UIException("CamerasController failed to load camera settings pop over", ex);
        }

        videoList.addListener((ListChangeListener.Change<? extends VideoModel> changes) -> {
            updateCameras(changes.getList());
        });
    }

    private void updateCameras(ObservableList<? extends VideoModel> modelList) {
        cameraList.getChildren().clear();
        for (var model : modelList) {
            var cameraView = new VideoView();
            cameraView.prefWidthProperty().bind(zoomSlider.valueProperty()
                    .multiply(cameraPane.widthProperty().subtract(2)));
            cameraView.prefHeightProperty().bind(zoomSlider.valueProperty()
                    .multiply(cameraPane.widthProperty().subtract(2))
                    .multiply(cameraView.frameHeightProperty())
                    .divide(cameraView.frameWidthProperty()));
            cameraView.setModel(model);
            cameraList.getChildren().add(cameraView);
        }
    }
}
