package com.rpicam.javafx;

import com.rpicam.exceptions.UIException;
import com.rpicam.javafx.models.CamerasPageModel;
import com.rpicam.scenes.ViewInfo;
import java.io.IOException;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
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

    private CamerasPageModel viewModel;
    private SimpleListProperty<ViewInfo> views = new SimpleListProperty<>(FXCollections.observableArrayList());

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

        cameraSettings.resultsProperty().addListener((obs, oldSettings, newSettings) -> {
            addCameraPopOver.hide();
            viewModel.addNewCamera(newSettings);
        });

        views.addListener((obs, oldViews, newViews) -> {
            cameraFlowPane.getChildren().clear();

            for (var view : newViews) {
                var cameraView = createCameraView();
                cameraView.getViewModel().init(view);
                cameraFlowPane.getChildren().add(cameraView);
            }
        });

        setViewModel(new CamerasPageModel());
    }

    @FXML
    private void onAddCameraClicked() {
        addCameraPopOver.show(addCameraBtn);
    }

    private CameraView createCameraView() {
        var cameraView = new CameraView();

        cameraView.prefWidthProperty().bind(zoomSlider.valueProperty()
                .multiply(cameraScrollPane.widthProperty().subtract(2)));
        cameraView.prefHeightProperty().bind(cameraView.prefWidthProperty()
                .multiply(cameraView.frameHeightProperty())
                .divide(cameraView.frameWidthProperty()));

        return cameraView;
    }

    public CamerasPageModel getViewModel() {
        return viewModel;
    }

    public void setViewModel(CamerasPageModel aViewModel) {
        viewModel = aViewModel;
        views.bind(viewModel.viewsProperty());
    }
}
