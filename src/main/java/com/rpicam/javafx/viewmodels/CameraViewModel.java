package com.rpicam.javafx.viewmodels;

import com.rpicam.cameras.ByteBufferImage;
import com.rpicam.cameras.ClassifierResult;
import com.rpicam.scenes.ViewInfo;
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
import com.rpicam.cameras.StatsResult;
import com.rpicam.javafx.App;
import com.rpicam.javafx.util.ViewModel;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class CameraViewModel implements ViewModel {
    private ViewInfo viewInfo;
    private CameraWorker camera;

    private PropertyChangeListener cameraClassifierListener;
    private PropertyChangeListener cameraFrameListener;
    private PropertyChangeListener cameraStatsListener;
    private SimpleObjectProperty<Image> frame = new SimpleObjectProperty<>();
    private SimpleListProperty<ClassifierResult> classifierResults = new SimpleListProperty<>(FXCollections.observableArrayList());
    private SimpleObjectProperty<StatsResult> statsResult = new SimpleObjectProperty<>();
    private SimpleBooleanProperty drawDetection = new SimpleBooleanProperty();
    private SimpleBooleanProperty drawStats = new SimpleBooleanProperty();

    public void init(ViewInfo info) {
        viewInfo = info;
        camera = App.cameraManager().getCamera(viewInfo.cameraUUID);
        drawDetection.set(viewInfo.drawDetection);
        drawStats.set(viewInfo.drawStats);
    }

    @Override
    public void onViewAdded() {
        // NOTE: Camera listeners are called from the camera thread rather than
        // the UI thread so use Platform.runLater() to avoid updating UI from a
        // non-JavaFX thread
        cameraFrameListener = event -> {
            var image = (ByteBufferImage) event.getNewValue();
            var jfxImage = wrapByteBufferImage(image);
            Platform.runLater(() -> {
                frame.set(jfxImage);
            });
        };
        cameraClassifierListener = event -> {
            var results = (ArrayList<ClassifierResult>) event.getNewValue();
            Platform.runLater(() -> {
                classifierResults.setAll(results);
            });
        };
        cameraStatsListener = event -> {
            var stats = (StatsResult) event.getNewValue();
            Platform.runLater(() -> {
                statsResult.set(stats);
            });
        };

        camera.addPropertyChangeListener("frame", cameraFrameListener);
        camera.addPropertyChangeListener("classifierResults", cameraClassifierListener);
        camera.addPropertyChangeListener("statsResult", cameraStatsListener);
    }

    @Override
    public void onViewRemoved() {
        camera.removePropertyChangeListener("classifierResults", cameraClassifierListener);
        camera.removePropertyChangeListener("frame", cameraFrameListener);
    }

    public ViewInfo getViewInfo() {
        return viewInfo;
    }

    private Image wrapByteBufferImage(ByteBufferImage image) {
        return new WritableImage(new PixelBuffer<>(image.width, image.height, image.buffer, PixelFormat.getByteBgraPreInstance()));
    }

    public List<ClassifierResult> getClassifierResults() {
        return classifierResults.get();
    }

    public ReadOnlyListProperty<ClassifierResult> classifierResultsProperty() {
        return classifierResults;
    }

    public StatsResult getStatsResult() {
        return statsResult.get();
    }

    public ReadOnlyObjectProperty<StatsResult> statsResultProperty() {
        return statsResult;
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
