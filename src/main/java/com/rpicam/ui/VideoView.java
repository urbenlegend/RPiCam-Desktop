package com.rpicam.ui;

import com.rpicam.video.ClassifierResult;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

public class VideoView extends StackPane {

    private Canvas classifierHud = new Canvas();
    private SimpleListProperty<ClassifierResult> classifierResults = new SimpleListProperty<>();
    private SimpleDoubleProperty frameHeight = new SimpleDoubleProperty(heightProperty().get());
    private ImageView frameView = new ImageView();
    private SimpleDoubleProperty frameWidth = new SimpleDoubleProperty(widthProperty().get());
    private VideoModel videoModel;

    public VideoView() {
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
        classifierResults.addListener((ListChangeListener.Change<? extends ClassifierResult> changes) -> {
            clearClassifiers();
            changes.getList().forEach(r -> {
                drawClassifier(r);
            });
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

    public ReadOnlyDoubleProperty frameHeightProperty() {
        return frameHeight;
    }

    public ReadOnlyDoubleProperty frameWidthProperty() {
        return frameWidth;
    }

    public void setModel(VideoModel aVideoModel) {
        videoModel = aVideoModel;
        frameView.imageProperty().bind(videoModel.frameProperty());
        classifierResults.bind(videoModel.classifierResultsProperty());
    }
}
