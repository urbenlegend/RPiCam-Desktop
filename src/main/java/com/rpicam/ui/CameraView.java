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
        double hudWidth = classifierHud.getWidth();
        double hudHeight = classifierHud.getHeight();
        double viewWidth = model.frameProperty().get().getWidth();
        double viewHeight = model.frameProperty().get().getHeight();

        var gc = classifierHud.getGraphicsContext2D();

        // Scale canvas drawing if image view is resized
        double scaleX = hudWidth / viewWidth;
        double scaleY = hudHeight / viewHeight;

        gc.setStroke(result.color);
        // TODO: Keep aspect ratio when scaling
        gc.strokeRect(result.x * scaleX, result.y * scaleY, result.w * scaleX, result.h * scaleY);
    }
    
    public void clearClassifiers() {
        var gc = classifierHud.getGraphicsContext2D();
        gc.clearRect(0, 0, classifierHud.getWidth(), classifierHud.getHeight());
    }
    
    public void processClassifierChange(ListChangeListener.Change<? extends ClassifierResult> results) {
        results.next();
        if (results.wasRemoved()) {
            clearClassifiers();
        }
        for (var r : results.getAddedSubList()) {
            drawClassifier(r);
        }
    }
}
