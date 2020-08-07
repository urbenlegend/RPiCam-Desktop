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
import com.rpicam.cameras.CameraWorker;
import com.rpicam.cameras.CameraListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class CameraViewModel implements CameraListener {
    private CameraWorker camera;

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
    public void onFrame(ByteBuffer buffer, int width, int height) {
        Platform.runLater(() -> {
            frame.set(wrapByteBuffer(buffer, width, height));
        });
    }

    private Image wrapByteBuffer(ByteBuffer buffer, int width, int height) {
        return new WritableImage(new PixelBuffer<>(width, height, buffer, PixelFormat.getByteBgraPreInstance()));
    }

    public List<ClassifierResult> getClassifierResults() {
        return classifierResults.get();
    }

    public ListProperty<ClassifierResult> classifierResultsProperty() {
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
