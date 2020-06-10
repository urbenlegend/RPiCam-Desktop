/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.ui;

import com.rpicam.video.VideoUtils;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.opencv.core.Mat;

/**
 *
 * @author benrx
 */
public class CameraModel {
    private SimpleObjectProperty<Image> frame;
    private SimpleListProperty<ClassifierResult> classifierResults;
    
    public CameraModel() {
        frame = new SimpleObjectProperty<>();
        classifierResults = new SimpleListProperty<>(FXCollections.observableArrayList());
    }
    
    public SimpleObjectProperty<Image> frameProperty() {
        return frame;
    }
    
    public SimpleListProperty<ClassifierResult> classifierResultsProperty() {
        return classifierResults;
    }
    
    public void setMat(Mat mat) {
        frame.set(VideoUtils.toJFXImage(mat));
    }
    
    public void addClassifierResult(String title, Color color, int x, int y, int w, int h) {
        var result = new ClassifierResult(title, color, x, y, w, h);
        classifierResults.get().add(result);
    }
    
    public void clearClassifierResults() {
        classifierResults.get().clear();
    }
}

class ClassifierResult {
    public final String title;
    public final Color color;
    public final int x, y, w, h;
    
    public ClassifierResult(String title, Color color, int x, int y, int w, int h) {
        this.title = title;
        this.color = color;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }
}