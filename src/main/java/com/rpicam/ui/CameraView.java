/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.ui;

import javafx.collections.ListChangeListener;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author benrx
 */
public class CameraView extends StackPane {
    private CameraModel model;
    private ImageView view;
    private Canvas classifierHud;
    
    public CameraView() {
        model = new CameraModel();
        view = new ImageView();
        classifierHud = new Canvas();
        view.imageProperty().bind(model.frameProperty());
        view.setPreserveRatio(true);
        view.fitWidthProperty().bind(widthProperty());
        view.fitHeightProperty().bind(heightProperty());
        classifierHud.widthProperty().bind(widthProperty());
        classifierHud.heightProperty().bind(heightProperty());
        model.classifierResultsProperty().addListener(this::processClassifierChange);
        
        getChildren().addAll(view, classifierHud);
    }
    
    public CameraModel getCameraModel() {
        return model;
    }
    
    public void drawClassifier(ClassifierResult result) {
        var gc = classifierHud.getGraphicsContext2D();
        gc.save();
        
        var cameraFrame = view.imageProperty().get();
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
        gc.setStroke(result.color);
        gc.strokeRect(result.x, result.y, result.w, result.h);
        
        // Draw classifier label
        gc.setTextAlign(TextAlignment.RIGHT);
        gc.setFill(result.color);
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
