package com.rpicam.ui;

import com.rpicam.ui.models.CameraModel;
import com.rpicam.video.ClassifierResult;
import com.rpicam.exceptions.UIException;
import java.io.IOException;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class CameraView extends StackPane {
    @FXML
    private ImageView frameView;
    @FXML
    private Canvas statsHud;
    @FXML
    private Canvas classifierHud;

    private SimpleObjectProperty<CameraModel> cameraModel = new SimpleObjectProperty<>();
    private SimpleDoubleProperty frameWidth = new SimpleDoubleProperty();
    private SimpleDoubleProperty frameHeight = new SimpleDoubleProperty();
    private SimpleListProperty<ClassifierResult> classifierResults = new SimpleListProperty<>();
    private SimpleBooleanProperty drawDetection = new SimpleBooleanProperty(true);
    private SimpleBooleanProperty drawStats = new SimpleBooleanProperty(true);

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

        statsHud.visibleProperty().bind(drawStats);
        classifierHud.visibleProperty().bind(drawDetection);

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

        // Bind model properties if we detect a new model is set
        cameraModel.addListener((obs, oldVal, newVal) -> {
            frameView.imageProperty().bind(newVal.frameProperty());
            classifierResults.bind(newVal.classifierResultsProperty());
            // TODO: Bind stats results
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public ReadOnlyDoubleProperty frameHeightProperty() {
        return frameHeight;
    }

    public ReadOnlyDoubleProperty frameWidthProperty() {
        return frameWidth;
    }

    public SimpleBooleanProperty drawDetectionProperty() {
        return drawDetection;
    }

    public SimpleBooleanProperty drawStatsProperty() {
        return drawStats;
    }

    public SimpleObjectProperty<CameraModel> cameraModelProperty() {
        return cameraModel;
    }
}
