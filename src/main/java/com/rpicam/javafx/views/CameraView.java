package com.rpicam.javafx.views;

import com.rpicam.javafx.viewmodels.CameraViewModel;
import com.rpicam.cameras.ClassifierResult;
import com.rpicam.cameras.StatsResult;
import com.rpicam.exceptions.UIException;
import com.rpicam.javafx.util.SelectMode;
import com.rpicam.javafx.util.Selectable;
import com.rpicam.javafx.util.SelectionGroup;
import com.rpicam.javafx.util.View;
import java.io.IOException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
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

public class CameraView extends StackPane implements View, Selectable {
    @FXML
    private ImageView frameView;
    @FXML
    private Canvas statsHud;
    @FXML
    private Canvas classifierHud;
    @FXML
    private Rectangle selectionBorder;

    private CameraViewModel viewModel = new CameraViewModel();
    private SimpleDoubleProperty frameWidth = new SimpleDoubleProperty(640);
    private SimpleDoubleProperty frameHeight = new SimpleDoubleProperty(480);
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
        selectionBorder.visibleProperty().bind(selected);

        parentProperty().addListener((obs, oldParent, newParent) -> {
            if (newParent != null) {
                viewModel.onViewAdded();
            }
            else {
                viewModel.onViewRemoved();
            }
        });

        bindData();
        setupEventHandlers();
    }

    private void bindData() {
        // Expose camera frame dimensions so that external code can resize
        // CameraView easily
        frameView.imageProperty().addListener((obs, oldImage, newImage) -> {
            frameWidth.set(newImage.getWidth());
            frameHeight.set(newImage.getHeight());
        });
        frameView.imageProperty().bind(viewModel.frameProperty());

        viewModel.classifierResultsProperty().addListener((obs, oldResults, newResults) -> {
            clearClassifierHud();
            newResults.forEach(r -> {
                drawClassifierHud(r);
            });
        });
        viewModel.statsResultProperty().addListener((obs, oldResults, newResults) -> {
            clearStatsHud();
            drawStatsHud(newResults);
        });

        // Show stats HUD when user enabled OR when there is no image available
        statsHud.visibleProperty().bind(viewModel.drawStatsProperty().or(frameView.imageProperty().isNull()));
        classifierHud.visibleProperty().bind(viewModel.drawDetectionProperty());
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

    private void clearClassifierHud() {
        var gc = classifierHud.getGraphicsContext2D();
        gc.clearRect(0, 0, classifierHud.getWidth(), classifierHud.getHeight());
    }

    private void drawClassifierHud(ClassifierResult result) {
        var gc = classifierHud.getGraphicsContext2D();
        gc.save();
        resizeCanvasGC(classifierHud);

        // Draw classifier bounding box
        Color classifierColor = Color.valueOf(result.color);
        gc.setStroke(classifierColor);
        gc.strokeRect(result.x, result.y, result.w, result.h);

        // Draw classifier label
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFill(classifierColor);
        gc.fillText(result.title, result.x + result.w, result.y + result.h + 15);

        gc.restore();
    }

    private void clearStatsHud() {
        var gc = statsHud.getGraphicsContext2D();
        gc.clearRect(0, 0, statsHud.getWidth(), statsHud.getHeight());
    }

    private void drawStatsHud(StatsResult result) {
        var gc = statsHud.getGraphicsContext2D();
        gc.save();
        resizeCanvasGC(statsHud);

        int upperLeftX = 5;
        int upperLeftY = 5;
        int lineSpacing = 15;
        gc.setFill(Color.valueOf("rgb(255, 255, 255)"));
        gc.setStroke(Color.valueOf("rgb(0, 0, 0)"));
        gc.fillText(result.cameraName, upperLeftX, upperLeftY + lineSpacing);
        gc.fillText(result.cameraStatus, upperLeftX, upperLeftY + lineSpacing * 2);
        gc.fillText(result.fps, upperLeftX, upperLeftY + lineSpacing * 3);
        gc.fillText(result.resolution, upperLeftX, upperLeftY + lineSpacing * 4);
        gc.fillText(result.networkStatus, upperLeftX, upperLeftY + lineSpacing * 5);
        gc.fillText(result.timestamp, upperLeftX, upperLeftY + lineSpacing * 6);

        gc.restore();
    }

    private void resizeCanvasGC(Canvas canvas) {
        var gc = canvas.getGraphicsContext2D();

        double imageWidth = frameWidth.get();
        double imageHeight = frameHeight.get();
        double hudWidth = canvas.getWidth();
        double hudHeight = canvas.getHeight();

        // Calculate aspect-ratio aware scale to match ImageView behavior
        double scaleX = hudWidth / imageWidth;
        double scaleY = hudHeight / imageHeight;
        double scaleFactor = Math.min(scaleX, scaleY);
        // Center the HUD drawing so it draws on top of ImageView after scaling
        if (scaleX > scaleY) {
            double viewWidth = imageWidth / imageHeight * hudHeight;
            gc.translate(hudWidth / 2 - viewWidth / 2, 0);
        } else {
            double viewHeight = imageHeight / imageWidth * hudWidth;
            gc.translate(0, hudHeight / 2 - viewHeight / 2);
        }
        gc.scale(scaleFactor, scaleFactor);
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
    public CameraViewModel getViewModel() {
        return viewModel;
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
