package com.rpicam.ui;

import com.rpicam.models.CameraViewModel;
import com.rpicam.video.ClassifierResult;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class CameraView extends StackPane {

    private Canvas classifierHud = new Canvas();
    private ImageView frameView = new ImageView();

    private CameraViewModel cameraModel;
    private SimpleListProperty<ClassifierResult> classifierResults = new SimpleListProperty<>();
    private SimpleDoubleProperty frameWidth = new SimpleDoubleProperty(widthProperty().get());
    private SimpleDoubleProperty frameHeight = new SimpleDoubleProperty(heightProperty().get());
    private SimpleBooleanProperty drawDetection = new SimpleBooleanProperty();
    private SimpleBooleanProperty drawStats = new SimpleBooleanProperty();

    public CameraView() {
        setMinSize(0, 0);
        frameView.setPreserveRatio(true);
        frameView.fitWidthProperty().bind(widthProperty());
        frameView.fitHeightProperty().bind(heightProperty());
        classifierHud.widthProperty().bind(widthProperty());
        classifierHud.heightProperty().bind(heightProperty());

        frameView.imageProperty().addListener((obs, oldVal, newVal) -> {
            frameWidth.set(newVal.getWidth());
            frameHeight.set(newVal.getHeight());
        });
        classifierResults.addListener((obs, oldVal, newVal) -> {
            if (!cameraModel.drawDetectionProperty().get()) {
                return;
            }

            clearClassifiers();
            newVal.forEach(r -> {
                drawClassifier(r);
            });
        });
        drawDetection.addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                clearClassifiers();
            }
        });
        drawStats.addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                clearStats();
            }
        });

        getChildren().addAll(frameView, classifierHud);
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
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

    public void setModel(CameraViewModel aCameraModel) {
        cameraModel = aCameraModel;
        frameView.imageProperty().bind(cameraModel.frameProperty());
        classifierResults.bind(cameraModel.classifierResultsProperty());
        drawDetection.bind(cameraModel.drawDetectionProperty());
        drawStats.bind(cameraModel.drawStatsProperty());
    }
}
