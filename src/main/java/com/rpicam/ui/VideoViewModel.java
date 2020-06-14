/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.ui;

import com.rpicam.video.ClassifierResult;
import com.rpicam.video.VideoUtils;
import java.util.ArrayList;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import org.opencv.core.Mat;

/**
 *
 * @author benrx
 */
public class VideoViewModel {
    private SimpleObjectProperty<Image> videoFrame;
    private SimpleListProperty<ClassifierResult> classifierResults;
    
    public VideoViewModel() {
        videoFrame = new SimpleObjectProperty<>();
        classifierResults = new SimpleListProperty<>(FXCollections.observableArrayList());
    }
    
    public SimpleObjectProperty<Image> frameProperty() {
        return videoFrame;
    }
    
    public SimpleListProperty<ClassifierResult> classifierResultsProperty() {
        return classifierResults;
    }
    
    public void setMat(Mat mat) {
        videoFrame.set(VideoUtils.toJFXImage(mat));
    }
    
    public void addClassifierResults(ArrayList<ClassifierResult> results) {
        classifierResults.get().addAll(results);
    }
    
    public void clearClassifierResults() {
        classifierResults.get().clear();
    }
}