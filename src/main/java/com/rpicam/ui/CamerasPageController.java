package com.rpicam.ui;

import com.rpicam.exceptions.UIException;
import java.io.IOException;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
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
    private FlowPane cameraList;
    @FXML
    private ScrollPane cameraPane;
    @FXML
    private Slider zoomSlider;
    private PopOver addCameraPopOver;
    private Parent addCameraSettings;
    private VideoListModel videoListModel;
    private SimpleListProperty<VideoModel> selection = new SimpleListProperty<>();
    private SimpleListProperty<VideoModel> videoList = new SimpleListProperty<>();

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
            cameraList.getChildren().clear();
            for (var model : changes.getList()) {
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
        });
    }

    public void setModel(VideoListModel aVideoListModel) {
        videoListModel = aVideoListModel;
        videoList.bind(videoListModel.videoListProperty());
        selection.bind(videoListModel.selectionProperty());
    }

    @FXML
    public void addCameraClicked() {
        addCameraPopOver.show(addCameraBtn);
    }
}
