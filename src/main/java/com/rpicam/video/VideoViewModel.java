package com.rpicam.video;

import java.util.ArrayList;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2BGRA;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import org.bytedeco.opencv.opencv_core.UMat;


public class VideoViewModel {
    private UMat bgraMat = new UMat();
    private SimpleObjectProperty<Image> videoFrame = new SimpleObjectProperty<>();
    private SimpleListProperty<ClassifierResult> classifierResults = new SimpleListProperty<>(FXCollections.observableArrayList());
    
    public SimpleObjectProperty<Image> frameProperty() {
        return videoFrame;
    }
    
    public SimpleListProperty<ClassifierResult> classifierResultsProperty() {
        return classifierResults;
    }
    
    public void setUMat(UMat mat) {
        cvtColor(mat, bgraMat, COLOR_BGR2BGRA);
        videoFrame.set(VideoUtils.wrapBgraUMat(bgraMat));
    }
    
    public void addClassifierResults(ArrayList<ClassifierResult> results) {
        classifierResults.get().addAll(results);
    }
    
    public void clearClassifierResults() {
        classifierResults.get().clear();
    }
}