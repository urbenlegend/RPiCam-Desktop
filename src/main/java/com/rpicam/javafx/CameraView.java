package com.rpicam.javafx;

import com.rpicam.javafx.models.CameraViewModel;
import com.rpicam.detection.ClassifierResult;
import com.rpicam.exceptions.UIException;
import com.rpicam.javafx.util.SelectMode;
import com.rpicam.javafx.util.Selectable;
import com.rpicam.javafx.util.SelectionGroup;
import com.rpicam.scenes.ViewInfo;
import java.io.IOException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;

public class CameraView extends StackPane implements Selectable {
    @FXML
    private ImageView frameView;
    @FXML
    private Canvas statsHud;
    @FXML
    private Canvas classifierHud;
    @FXML
    private Rectangle selectionBorder;

    private SimpleObjectProperty<ViewInfo> viewInfo = new SimpleObjectProperty<>();
    private SimpleObjectProperty<CameraViewModel> viewModel = new SimpleObjectProperty<>();
    private SimpleDoubleProperty frameWidth = new SimpleDoubleProperty();
    private SimpleDoubleProperty frameHeight = new SimpleDoubleProperty();
    private SimpleListProperty<ClassifierResult> classifierResults = new SimpleListProperty<>();
    private SimpleBooleanProperty selected = new SimpleBooleanProperty();
    private SimpleObjectProperty<SelectionGroup> selectionGroup = new SimpleObjectProperty<>();

    public CameraView() {
        final String FXML_PATH = "CameraView.fxml";
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        }
        catch (IOException ex) {
            throw new UIException("Failed to load " + FXML_PATH, ex);
        }
    }

    @FXML
    public void initialize() {
        // Make all the components have the same size as the main component
        frameView.fitWidthProperty().bind(widthProperty());
        frameView.fitHeightProperty().bind(heightProperty());
        statsHud.widthProperty().bind(widthProperty());
        statsHud.heightProperty().bind(heightProperty());
        classifierHud.widthProperty().bind(widthProperty());
        classifierHud.heightProperty().bind(heightProperty());
        selectionBorder.widthProperty().bind(widthProperty());
        selectionBorder.heightProperty().bind(heightProperty());
        
        bindData();
        setupEventHandlers();
    }
    
    private void bindData() {
        // Expose camera frame dimensions so that
        // external code can resize CameraView easily
        frameView.imageProperty().addListener((obs, oldVal, newVal) -> {
            frameWidth.set(newVal.getWidth());
            frameHeight.set(newVal.getHeight());
        });
        // TODO: Add listener for stats results
        // Draw classifiers whenever we get new results
        classifierResults.addListener((obs, oldVal, newVal) -> {
            clearClassifiers();
            newVal.forEach(r -> {
                drawClassifier(r);
            });
        });
        
        selectionBorder.visibleProperty().bind(selected);

        // Bind model properties if we detect a new model is set
        viewModel.addListener((obs, oldVal, newVal) -> {
            frameView.imageProperty().bind(newVal.frameProperty());
            // TODO: Bind stats results
            classifierResults.bind(newVal.classifierResultsProperty());
            statsHud.visibleProperty().bind(newVal.drawStatsProperty());
            classifierHud.visibleProperty().bind(newVal.drawDetectionProperty());
        });

        viewInfo.addListener((obs, oldVal, newVal) -> {
            var newViewModel = new CameraViewModel();
            newViewModel.init(newVal);
            viewModel.set(newViewModel);
        });
    }
    
    private void setupEventHandlers() {
        addEventHandler(MouseEvent.MOUSE_PRESSED, (event) -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                if (event.isShiftDown()) {
                    select(SelectMode.APPEND);
                }
                else {
                    select(SelectMode.SINGLE);
                }
            }
            event.consume();
        });
    }

    public void clearClassifiers() {
        var gc = classifierHud.getGraphicsContext2D();
        gc.clearRect(0, 0, classifierHud.getWidth(), classifierHud.getHeight());
    }

    public void drawClassifier(ClassifierResult result) {
        var gc = classifierHud.getGraphicsContext2D();
        gc.save();

        double imageWidth = frameWidth.get();
        double imageHeight = frameHeight.get();
        double hudWidth = classifierHud.getWidth();
        double hudHeight = classifierHud.getHeight();

        // Calculate aspect-ratio aware scale to match ImageView behavior
        double scaleX = hudWidth / imageWidth;
        double scaleY = hudHeight / imageHeight;
        double scaleFactor = Math.min(scaleX, scaleY);
        // Centers the HUD drawing so it draws on top of ImageView after scaling
        if (scaleX > scaleY) {
            double viewWidth = imageWidth / imageHeight * hudHeight;
            gc.translate(hudWidth / 2 - viewWidth / 2, 0);
        } else {
            double viewHeight = imageHeight / imageWidth * hudWidth;
            gc.translate(0, hudHeight / 2 - viewHeight / 2);
        }
        gc.scale(scaleFactor, scaleFactor);

        // Draw classifier bounding box
        Color boxColor = Color.valueOf(result.color);
        gc.setStroke(boxColor);
        gc.strokeRect(result.x, result.y, result.w, result.h);

        // Draw classifier label
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFill(boxColor);
        gc.fillText(result.title, result.x + result.w, result.y + result.h + 15);

        gc.restore();
    }

    private void clearStats() {
        var gc = statsHud.getGraphicsContext2D();
        gc.clearRect(0, 0, statsHud.getWidth(), statsHud.getHeight());
    }

    private void drawStats() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public ViewInfo getViewInfo() {
        return viewInfo.get();
    }
    
    public void setViewInfo(ViewInfo info) {
        viewInfo.set(info);
    }
    
    public ObjectProperty<ViewInfo> viewInfoProperty() {
        return viewInfo;
    }
    
    public CameraViewModel getViewModel() {
        return viewModel.get();
    }

    public void setViewModel(CameraViewModel aViewModel) {
        viewModel.set(aViewModel);
    }

    public ObjectProperty<CameraViewModel> viewModelProperty() {
        return viewModel;
    }

    public double getFrameWidth() {
        return frameWidth.get();
    }

    public ReadOnlyDoubleProperty frameWidthProperty() {
        return frameWidth;
    }

    public double getFrameHeight() {
        return frameHeight.get();
    }

    public ReadOnlyDoubleProperty frameHeightProperty() {
        return frameHeight;
    }

    @Override
    public boolean isSelected() {
        return selected.get();
    }
    
    @Override
    public void select(SelectMode mode) {        
        if (selectionGroup.get() != null) {
            selectionGroup.get().select(this, mode);
        }
    }

    @Override
    public BooleanProperty selectedProperty() {
        return selected;
    }

    @Override
    public SelectionGroup getSelectionGroup() {
        return selectionGroup.get();
    }

    @Override
    public void setSelectionGroup(SelectionGroup group) {
        selectionGroup.set(group);
    }

    @Override
    public ObjectProperty<SelectionGroup> selectionGroupProperty() {
        return selectionGroup;
    }
}
