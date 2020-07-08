package com.rpicam.models;

import com.rpicam.video.ClassifierResult;
import java.nio.ByteBuffer;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import static org.bytedeco.opencv.global.opencv_core.ACCESS_READ;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2BGRA;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.UMat;
import com.rpicam.video.CameraWorker;

public class CameraModel {

    private CameraWorker worker;
    private final UMat bgraMat = new UMat();

    private SimpleObjectProperty<Image> frame = new SimpleObjectProperty<>();
    private SimpleListProperty<ClassifierResult> classifierResults = new SimpleListProperty<>(FXCollections.observableArrayList());
    private SimpleBooleanProperty drawDetection = new SimpleBooleanProperty();
    private SimpleBooleanProperty drawStats = new SimpleBooleanProperty();

    public CameraModel(CameraWorker aWorker) {
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
                frame.set(wrapBgraUMat(bgraMat));
            }
        });
    }

    public static Image wrapBgraUMat(UMat bgraMat) {
        try ( Mat tempMat = bgraMat.getMat(ACCESS_READ)) {
            var pixelBuf = new PixelBuffer<ByteBuffer>(tempMat.cols(), tempMat.rows(), tempMat.createBuffer(), PixelFormat.getByteBgraPreInstance());
            return new WritableImage(pixelBuf);
        }
    }

    public SimpleListProperty<ClassifierResult> classifierResultsProperty() {
        return classifierResults;
    }

    public ObjectProperty<Image> frameProperty() {
        return frame;
    }

    public SimpleBooleanProperty drawDetectionProperty() {
        return drawDetection;
    }

    public SimpleBooleanProperty drawStatsProperty() {
        return drawStats;
    }
}
