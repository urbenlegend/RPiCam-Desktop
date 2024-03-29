package com.rpicam.javafx.views;

import com.rpicam.javafx.viewmodels.CameraViewModel;
import com.rpicam.detection.ClassifierResult;
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
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;

public class CameraView extends StackPane implements View, Selectable {
    public static final int DEFAULT_WIDTH = 640;
    public static final int DEFAULT_HEIGHT = 480;

    @FXML
    private BorderPane noVideoPane;
    @FXML
    private ImageView frameView;
    @FXML
    private Canvas classifierHud;
    @FXML
    private AnchorPane statsHud;
    @FXML
    private HBox statsUpperLeft;
    @FXML
    private HBox statsLowerLeft;
    @FXML
    private Label cameraNameLabel;
    @FXML
    private Label videoStatusLabel;
    @FXML
    private Label cameraStatusLabel;
    @FXML
    private Label timestampLabel;
    @FXML
    private Rectangle selectionBorder;

    private CameraViewModel viewModel = new CameraViewModel();
    private SimpleDoubleProperty frameWidth = new SimpleDoubleProperty(DEFAULT_WIDTH);
    private SimpleDoubleProperty frameHeight = new SimpleDoubleProperty(DEFAULT_HEIGHT);
    private SimpleBooleanProperty selected = new SimpleBooleanProperty();
    private SimpleObjectProperty<SelectionGroup> selectionGroup = new SimpleObjectProperty<>();

    public CameraView() {
        final String FXML_PATH = "CameraView.fxml";
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        } catch (IOException ex) {
            throw new UIException("Failed to load " + FXML_PATH, ex);
        }
    }

    @FXML
    public void initialize() {
        // Make all the components have the same size as the main component
        frameView.fitWidthProperty().bind(widthProperty());
        frameView.fitHeightProperty().bind(heightProperty());
        classifierHud.widthProperty().bind(widthProperty());
        classifierHud.heightProperty().bind(heightProperty());
        selectionBorder.widthProperty().bind(widthProperty());
        selectionBorder.heightProperty().bind(heightProperty());
        selectionBorder.visibleProperty().bind(selected);

        // Calculate scale factors for scaling stats HUD. Scale by
        // default width and height so that it isn't too small for HD
        var statsHudScale = new SimpleDoubleProperty();
        statsHudScale.bind(widthProperty().divide(DEFAULT_WIDTH));

        // Make stats HUD elements scale with CameraView size
        statsUpperLeft.scaleXProperty().bind(statsHudScale);
        statsUpperLeft.scaleYProperty().bind(statsHudScale);
        statsLowerLeft.scaleXProperty().bind(statsHudScale);
        statsLowerLeft.scaleYProperty().bind(statsHudScale);
        statsUpperLeft.translateXProperty().bind(statsUpperLeft.widthProperty().subtract(statsUpperLeft.widthProperty().multiply(statsHudScale)).divide(2).negate());
        statsUpperLeft.translateYProperty().bind(statsUpperLeft.heightProperty().subtract(statsUpperLeft.heightProperty().multiply(statsHudScale)).divide(2).negate());
        statsLowerLeft.translateXProperty().bind(statsLowerLeft.widthProperty().subtract(statsLowerLeft.widthProperty().multiply(statsHudScale)).divide(2).negate());
        statsLowerLeft.translateYProperty().bind(statsLowerLeft.heightProperty().subtract(statsLowerLeft.heightProperty().multiply(statsHudScale)).divide(2));

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

        cameraNameLabel.textProperty().bind(viewModel.cameraNameProperty());
        videoStatusLabel.textProperty().bind(viewModel.videoStatusProperty());
        cameraStatusLabel.textProperty().bind(viewModel.cameraStatusProperty());
        timestampLabel.textProperty().bind(viewModel.timestampProperty());

        // Show stats HUD when user enabled OR when there is no image available
        statsHud.visibleProperty().bind(viewModel.drawStatsProperty().or(frameView.imageProperty().isNull()));
        classifierHud.visibleProperty().bind(viewModel.drawDetectionProperty());
        noVideoPane.visibleProperty().bind(frameView.imageProperty().isNull());
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
}
