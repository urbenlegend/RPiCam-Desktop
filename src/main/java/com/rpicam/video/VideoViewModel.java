package com.rpicam.video;

import java.util.ArrayList;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.UMat;


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
    
    public void setMat(UMat mat) {
        // TODO: Figure out a way to not incur the buffer copy to reduce CPU
        Mat tempMat = new Mat();
        mat.copyTo(tempMat);
        videoFrame.set(VideoUtils.toJFXImage(tempMat));
    }
    
    public void addClassifierResults(ArrayList<ClassifierResult> results) {
        classifierResults.get().addAll(results);
    }
    
    public void clearClassifierResults() {
        classifierResults.get().clear();
    }
}