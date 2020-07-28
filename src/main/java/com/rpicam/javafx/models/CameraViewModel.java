package com.rpicam.javafx.models;

import com.rpicam.detection.ClassifierResult;
import com.rpicam.javafx.App;
import com.rpicam.scenes.ViewInfo;
import java.nio.ByteBuffer;
import java.util.List;
import javafx.application.Platform;
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
import com.rpicam.cameras.CameraWorker;
import com.rpicam.cameras.CameraListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class CameraViewModel implements CameraListener {
    private CameraWorker camera;
    private final UMat bgraMat = new UMat();

    private SimpleObjectProperty<Image> frame = new SimpleObjectProperty<>();
    private SimpleListProperty<ClassifierResult> classifierResults = new SimpleListProperty<>(FXCollections.observableArrayList());
    private SimpleBooleanProperty drawDetection = new SimpleBooleanProperty();
    private SimpleBooleanProperty drawStats = new SimpleBooleanProperty();

    public void init(ViewInfo info) {
        camera = App.cameraManager().getCamera(info.cameraUUID);
        drawDetection.set(info.drawDetection);
        drawStats.set(info.drawStats);
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

    @Override
    public void onFrame(ByteBuffer buffer, int width, int height) {
        Platform.runLater(() -> {
            frame.set(wrapByteBuffer(buffer, width, height));
        });
    }

    private Image wrapBgraUMat(UMat bgraMat) {
        try (Mat tempMat = bgraMat.getMat(ACCESS_READ)) {
            var pixelBuf = new PixelBuffer<ByteBuffer>(tempMat.cols(), tempMat.rows(), tempMat.createBuffer(), PixelFormat.getByteBgraPreInstance());
            return new WritableImage(pixelBuf);
        }
    }

    private Image wrapByteBuffer(ByteBuffer buffer, int width, int height) {
        return new WritableImage(new PixelBuffer<>(width, height, buffer, PixelFormat.getByteBgraPreInstance()));
    }

    public List<ClassifierResult> getClassifierResults() {
        return classifierResults.get();
    }

    public SimpleListProperty<ClassifierResult> classifierResultsProperty() {
        return classifierResults;
    }

    public Image getFrame() {
        return frame.get();
    }

    public ReadOnlyObjectProperty<Image> frameProperty() {
        return frame;
    }

    public boolean isDrawDetection() {
        return drawDetection.get();
    }

    public void setDrawDetection(boolean enableDrawDetection) {
        drawDetection.set(enableDrawDetection);
    }

    public BooleanProperty drawDetectionProperty() {
        return drawDetection;
    }

    public boolean isDrawStats() {
        return drawStats.get();
    }

    public void setDrawStats(boolean enableDrawStats) {
        drawStats.set(enableDrawStats);
    }

    public BooleanProperty drawStatsProperty() {
        return drawStats;
    }
}
