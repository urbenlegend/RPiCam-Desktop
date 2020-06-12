/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.ui;

import com.rpicam.video.ClassifierResult;
import javafx.collections.ListChangeListener;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author benrx
 */
public class VideoView extends StackPane {
    private VideoModel uiModel;
    private ImageView frameView;
    private Canvas classifierHud;
    
    public VideoView() {
        uiModel = new VideoModel();
        frameView = new ImageView();
        classifierHud = new Canvas();
        frameView.imageProperty().bind(uiModel.frameProperty());
        frameView.setPreserveRatio(true);
        frameView.fitWidthProperty().bind(widthProperty());
        frameView.fitHeightProperty().bind(heightProperty());
        classifierHud.widthProperty().bind(widthProperty());
        classifierHud.heightProperty().bind(heightProperty());
        uiModel.classifierResultsProperty().addListener(this::processClassifierChange);
        
        getChildren().addAll(frameView, classifierHud);
    }
    
    public VideoModel getCameraModel() {
        return uiModel;
    }
    
    public void drawClassifier(ClassifierResult result) {
        var gc = classifierHud.getGraphicsContext2D();
        gc.save();
        
        var cameraFrame = frameView.imageProperty().get();
        double imageWidth = cameraFrame.getWidth();
        double imageHeight = cameraFrame.getHeight();
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
        }
        else {
            double viewHeight = imageHeight / imageWidth * hudWidth;
            gc.translate(0, hudHeight / 2 - viewHeight / 2);
        }
        gc.scale(scaleFactor, scaleFactor);

        // Draw classifier bounding box
        Color boxColor = Color.rgb(result.r, result.g, result.b);
        gc.setStroke(boxColor);
        gc.strokeRect(result.x, result.y, result.w, result.h);
        
        // Draw classifier label
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFill(boxColor);
        gc.fillText(result.title, result.x + result.w, result.y + result.h + 15);
        
        gc.restore();
    }
    
    public void clearClassifiers() {
        var gc = classifierHud.getGraphicsContext2D();
        gc.clearRect(0, 0, classifierHud.getWidth(), classifierHud.getHeight());
    }
    
    private void processClassifierChange(ListChangeListener.Change<? extends ClassifierResult> results) {
        results.next();
        if (results.wasRemoved()) {
            clearClassifiers();
        }
        for (var r : results.getAddedSubList()) {
            drawClassifier(r);
        }
    }
}
