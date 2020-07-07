package com.rpicam.ui;

import com.rpicam.util.VideoUtils;
import com.rpicam.video.ClassifierResult;
import com.rpicam.video.VideoWorker;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2BGRA;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import org.bytedeco.opencv.opencv_core.UMat;

public class VideoModel {

    private VideoWorker worker;
    private final UMat bgraMat = new UMat();
    private SimpleListProperty<ClassifierResult> classifierResults = new SimpleListProperty<>(FXCollections.observableArrayList());
    private SimpleObjectProperty<Image> frame = new SimpleObjectProperty<>();

    public VideoModel(VideoWorker aWorker) {
        worker = aWorker;
    }

    public void updateClassifierResultsLater(List<ClassifierResult> results) {
        Platform.runLater(() -> {
            classifierResults.setAll(results);
        });
    }

    public void updateFrameLater(UMat mat) {
        synchronized (bgraMat) {
            cvtColor(mat, bgraMat, COLOR_BGR2BGRA);
        }

        Platform.runLater(() -> {
            synchronized (bgraMat) {
                frame.set(VideoUtils.wrapBgraUMat(bgraMat));
            }
        });
    }

    public SimpleListProperty<ClassifierResult> classifierResultsProperty() {
        return classifierResults;
    }

    public ObjectProperty<Image> frameProperty() {
        return frame;
    }
}
