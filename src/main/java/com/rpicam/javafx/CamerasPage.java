package com.rpicam.javafx;

import com.rpicam.exceptions.UIException;
import com.rpicam.javafx.models.CamerasPageModel;
import com.rpicam.javafx.util.SelectionGroup;
import com.rpicam.scenes.ViewInfo;
import java.io.IOException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
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
    private CameraSettings addCameraSettings;
    private SelectionGroup cameraSelectGroup = new SelectionGroup();

    private SimpleObjectProperty<CamerasPageModel> viewModel = new SimpleObjectProperty<>();
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
        addCameraSettings = new CameraSettings();
        addCameraPopOver = new PopOver(addCameraSettings);
        addCameraPopOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_LEFT);
        
        bindData();
        setupEventHandlers();
    }
    
    private void bindData() {
        addCameraSettings.resultsProperty().addListener((obs, oldSettings, newSettings) -> {
            addCameraPopOver.hide();
            cameraSelectGroup.unselectAll();
            getViewModel().addNewCamera(newSettings);
        });

        views.addListener((obs, oldViews, newViews) -> {
            cameraFlowPane.getChildren().clear();

            for (var view : newViews) {
                var cameraView = createCameraView();
                cameraView.setViewInfo(view);
                cameraFlowPane.getChildren().add(cameraView);
            }
        });

        viewModel.addListener((obs, oldModel, newModel) -> {
            views.bind(newModel.viewsProperty());
        });

        viewModel.set(new CamerasPageModel());
    }
    
    private void setupEventHandlers() {
        cameraScrollPane.addEventHandler(MouseEvent.MOUSE_PRESSED, (event) -> {
            cameraSelectGroup.unselectAll();
        });
    }

    @FXML
    private void onAddCameraClicked() {
        addCameraPopOver.show(addCameraBtn);
    }
    
    @FXML
    private void onRemoveCameraClicked() {
        for (var selected : cameraSelectGroup.getSelectedItems()) {
            var cameraView = (CameraView) selected;
            getViewModel().removeCameraByViewInfo(cameraView.getViewInfo());
        }
        cameraSelectGroup.unselectAll();
    }

    private CameraView createCameraView() {
        var cameraView = new CameraView();

        cameraView.prefWidthProperty().bind(zoomSlider.valueProperty()
                .multiply(cameraScrollPane.widthProperty().subtract(2)));
        cameraView.prefHeightProperty().bind(cameraView.prefWidthProperty()
                .multiply(cameraView.frameHeightProperty())
                .divide(cameraView.frameWidthProperty()));
        
        cameraView.setSelectionGroup(cameraSelectGroup);

        return cameraView;
    }

    public CamerasPageModel getViewModel() {
        return viewModel.get();
    }

    public void setViewModel(CamerasPageModel aViewModel) {
        viewModel.set(aViewModel);
    }

    public ObjectProperty<CamerasPageModel> viewModelProperty() {
        return viewModel;
    }
}
