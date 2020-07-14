package com.rpicam.ui.models;

import com.rpicam.dto.video.ClassifierResult;
import java.nio.ByteBuffer;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
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
import com.rpicam.video.CameraListener;

public class CameraModel implements CameraListener {

    private CameraWorker camera;
    private final UMat bgraMat = new UMat();

    private SimpleObjectProperty<Image> frame = new SimpleObjectProperty<>();
    private SimpleListProperty<ClassifierResult> classifierResults = new SimpleListProperty<>(FXCollections.observableArrayList());

    public CameraModel(CameraWorker aCamera) {
        camera = aCamera;
        camera.addWeakListener(this);
    }

    @Override
    public void onClassifierResults(List<ClassifierResult> results) {
        Platform.runLater(() -> {
            classifierResults.setAll(results);
        });
    }

    @Override
    public void onFrame(UMat mat) {
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
}
