package com.rpicam.ui;

import com.rpicam.video.ClassifierResult;
import java.util.List;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;


public class VideoViewModel {
    private SimpleObjectProperty<Image> frame = new SimpleObjectProperty<>();
    private SimpleListProperty<ClassifierResult> classifierResults = new SimpleListProperty<>(FXCollections.synchronizedObservableList(FXCollections.observableArrayList()));

    public SimpleObjectProperty<Image> frameProperty() {
        return frame;
    }

    public SimpleListProperty<ClassifierResult> classifierResultsProperty() {
        return classifierResults;
    }

    public void addClassifierResults(List<ClassifierResult> results) {
        classifierResults.get().addAll(results);
    }

    public void clearClassifierResults() {
        classifierResults.get().clear();
    }
}