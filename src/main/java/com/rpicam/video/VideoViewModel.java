package com.rpicam.video;

import java.util.ArrayList;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import org.opencv.core.Mat;


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