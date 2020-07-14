package com.rpicam.ui;

import com.rpicam.ui.models.CameraManagerModel;
import com.rpicam.exceptions.UIException;
import com.rpicam.ui.models.CameraModel;
import com.rpicam.video.CameraWorker;
import com.rpicam.video.OCVLocalCamera;
import com.rpicam.video.OCVStreamCamera;
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
    private CameraSettingsController addCameraController;

    private CameraManagerModel cameraMgrModel;
    private SimpleListProperty<CameraWorker> selection = new SimpleListProperty<>();
    private SimpleListProperty<CameraWorker> cameraList = new SimpleListProperty<>();

    @FXML
    public void initialize() {
        try {
            FXMLLoader cameraSettingsLoader = new FXMLLoader(getClass().getResource("CameraSettings.fxml"));
            addCameraSettings = cameraSettingsLoader.load();
            addCameraController = cameraSettingsLoader.getController();
            addCameraPopOver = new PopOver(addCameraSettings);
            addCameraPopOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_LEFT);
        } catch (IOException ex) {
            throw new UIException("Failed to load camera settings pop over", ex);
        }

        cameraList.addListener((obs, oldVal, newVal) -> {
            cameraFlowPane.getChildren().clear();
            for (var camera : newVal) {
                try {
                    FXMLLoader cameraViewLoader = new FXMLLoader(getClass().getResource("CameraView.fxml"));
                    Parent cameraView = cameraViewLoader.load();
                    CameraViewController viewController = cameraViewLoader.getController();
                    viewController.prefWidthProperty().bind(zoomSlider.valueProperty()
                            .multiply(cameraScrollPane.widthProperty().subtract(2)));
                    viewController.prefHeightProperty().bind(zoomSlider.valueProperty()
                            .multiply(cameraScrollPane.widthProperty().subtract(2))
                            .multiply(viewController.frameHeightProperty())
                            .divide(viewController.frameWidthProperty()));
                    var cameraModel = new CameraModel(camera);
                    viewController.cameraModelProperty().set(cameraModel);
                    cameraFlowPane.getChildren().add(cameraView);
                } catch (IOException ex) {
                    throw new UIException("Failed to load camera view UI", ex);
                }
            }
        });

        addCameraController.resultsProperty().addListener((obs, oldVal, newVal) -> {
            CameraWorker camera = null;
            switch (newVal.get("type")) {
                case "local" -> {
                    var newCamera = new OCVLocalCamera();
                    var options = newCamera.getParameters();
                    options.camIndex = Integer.parseInt(newVal.get("camIndex"));
                    options.captureApi = newVal.get("captureApi");
                    options.widthRes = Integer.parseInt(newVal.get("widthRes"));
                    options.heightRes = Integer.parseInt(newVal.get("heightRes"));
                    options.capRate = 1000 / Integer.parseInt(newVal.get("capFPS"));
                    options.procRate = 1000 / Integer.parseInt(newVal.get("procFPS"));
                    newCamera.setParameters(options);
                    camera = newCamera;
                }
                case "url" -> {
                    var newCamera = new OCVStreamCamera();
                    var options = newCamera.getParameters();
                    options.url = newVal.get("path");
                    options.captureApi = newVal.get("captureApi");
                    options.widthRes = Integer.parseInt(newVal.get("widthRes"));
                    options.heightRes = Integer.parseInt(newVal.get("heightRes"));
                    options.capRate = 1000 / Integer.parseInt(newVal.get("capFPS"));
                    options.procRate = 1000 / Integer.parseInt(newVal.get("procFPS"));
                    newCamera.setParameters(options);
                    camera = newCamera;
                }
            }

            try {
                camera.start();
                cameraMgrModel.addCamera(camera);
            }
            catch (Exception ex) {
                // TODO: Display error dialog
                ex.printStackTrace();
            }

            addCameraPopOver.hide();
        });

        cameraMgrModel = MainApp.getCameraManager().getViewModel();
        cameraList.bind(cameraMgrModel.cameraListProperty());
        selection.bind(cameraMgrModel.selectionProperty());
    }

    @FXML
    public void addCameraClicked() {
        addCameraPopOver.show(addCameraBtn);
    }
}
