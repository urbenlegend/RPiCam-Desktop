package com.rpicam.ui;

import com.rpicam.exceptions.UIException;
import com.rpicam.scenes.ViewInfo;
import com.rpicam.ui.models.CameraModel;
import com.rpicam.ui.models.SceneModel;
import com.rpicam.video.CameraWorker;
import com.rpicam.video.OCVLocalCamera;
import com.rpicam.video.OCVStreamCamera;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import org.controlsfx.control.PopOver;

public class CamerasPage extends BorderPane {
    @FXML
    private Button addCameraBtn;
    @FXML
    private FlowPane cameraFlowPane;
    @FXML
    private ScrollPane cameraScrollPane;
    @FXML
    private Slider zoomSlider;

    private PopOver addCameraPopOver;
    private CameraSettings cameraSettings;

    private SimpleObjectProperty<SceneModel> sceneModel = new SimpleObjectProperty<>();
    private SimpleListProperty<ViewInfo> views = new SimpleListProperty<>();

    public CamerasPage() {
        final String FXML_PATH = "CamerasPage.fxml";
        try {
            var loader = new FXMLLoader(getClass().getResource(FXML_PATH));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        } catch (IOException ex) {
            throw new UIException("Failed to load " + FXML_PATH, ex);
        }
    }

    @FXML
    public void initialize() {
        cameraSettings = new CameraSettings();
        addCameraPopOver = new PopOver(cameraSettings);
        addCameraPopOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_LEFT);

        cameraSettings.resultsProperty().addListener((obs, oldVal, newVal) -> {
            addCameraPopOver.hide();
            addNewCamera(newVal);
        });

        views.addListener((obs, oldVal, newVal) -> {
            var cameraManager = App.getCameraManager();
            cameraFlowPane.getChildren().clear();

            for (var view : newVal) {
                var camera = cameraManager.getCamera(view.cameraUUID);
                var cameraView = createViewFromCamera(camera);
                cameraView.drawStatsProperty().set(view.drawStats);
                cameraView.drawDetectionProperty().set(view.drawDetection);
                cameraFlowPane.getChildren().add(cameraView);
            }
        });

        sceneModel.addListener((obs, oldVal, newVal) -> {
            views.bind(newVal.viewsProperty());
        });
    }

    private void addNewCamera(Map<String, String> cameraPropMap) {
        CameraWorker camera = null;
        switch (cameraPropMap.get("type")) {
            case "local" -> {
                var newCamera = new OCVLocalCamera();
                var config = newCamera.toConfig();
                config.camIndex = Integer.parseInt(cameraPropMap.get("camIndex"));
                config.captureApi = cameraPropMap.get("captureApi");
                config.widthRes = Integer.parseInt(cameraPropMap.get("widthRes"));
                config.heightRes = Integer.parseInt(cameraPropMap.get("heightRes"));
                config.capRate = 1000 / Integer.parseInt(cameraPropMap.get("capFPS"));
                config.procRate = 1000 / Integer.parseInt(cameraPropMap.get("procFPS"));
                newCamera.fromConfig(config);
                camera = newCamera;
            }
            case "path" -> {
                var newCamera = new OCVStreamCamera();
                var config = newCamera.toConfig();
                config.url = cameraPropMap.get("url");
                config.captureApi = cameraPropMap.get("captureApi");
                config.widthRes = Integer.parseInt(cameraPropMap.get("widthRes"));
                config.heightRes = Integer.parseInt(cameraPropMap.get("heightRes"));
                config.capRate = 1000 / Integer.parseInt(cameraPropMap.get("capFPS"));
                config.procRate = 1000 / Integer.parseInt(cameraPropMap.get("procFPS"));
                newCamera.fromConfig(config);
                camera = newCamera;
            }
        }

        try {
            camera.start();
            var cameraManager = App.getCameraManager();
            UUID cameraUUID = cameraManager.addCamera(camera);
            var viewInfo = new ViewInfo();
            viewInfo.cameraUUID = cameraUUID;
            viewInfo.drawStats = Boolean.parseBoolean(cameraPropMap.get("drawStats"));
            viewInfo.drawDetection = Boolean.parseBoolean(cameraPropMap.get("drawDetection"));
            sceneModel.get().addViewInfo(viewInfo);
        }
        catch (Exception ex) {
            // TODO: Display error dialog
            ex.printStackTrace();
        }
    }

    private CameraView createViewFromCamera(CameraWorker camera) {
        var cameraModel = new CameraModel(camera);
        var cameraView = new CameraView();
        cameraView.cameraModelProperty().set(cameraModel);

        cameraView.prefWidthProperty().bind(zoomSlider.valueProperty()
                .multiply(cameraScrollPane.widthProperty().subtract(2)));
        cameraView.prefHeightProperty().bind(zoomSlider.valueProperty()
                .multiply(cameraScrollPane.widthProperty().subtract(2))
                .multiply(cameraView.frameHeightProperty())
                .divide(cameraView.frameWidthProperty()));

        return cameraView;
    }

    @FXML
    private void onAddCameraClicked() {
        addCameraPopOver.show(addCameraBtn);
    }

    public SimpleObjectProperty<SceneModel> sceneModelProperty() {
        return sceneModel;
    }
}
