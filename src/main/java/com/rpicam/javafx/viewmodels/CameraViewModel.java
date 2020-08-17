package com.rpicam.javafx.viewmodels;

import com.rpicam.cameras.ByteBufferImage;
import com.rpicam.detection.ClassifierResult;
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
import com.rpicam.javafx.App;
import com.rpicam.javafx.util.ViewModel;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class CameraViewModel implements ViewModel {
    private ViewInfo viewInfo;
    private CameraWorker camera;

    private SimpleObjectProperty<Image> frame = new SimpleObjectProperty<>();
    private SimpleListProperty<ClassifierResult> classifierResults = new SimpleListProperty<>(FXCollections.observableArrayList());
    private SimpleBooleanProperty drawDetection = new SimpleBooleanProperty();
    private SimpleBooleanProperty drawStats = new SimpleBooleanProperty();
    private SimpleStringProperty cameraName = new SimpleStringProperty();
    private SimpleStringProperty videoQuality = new SimpleStringProperty();
    private SimpleStringProperty cameraStatus = new SimpleStringProperty();
    private SimpleStringProperty timestamp = new SimpleStringProperty();
    private PropertyChangeListener cameraFrameListener;
    private PropertyChangeListener cameraClassifierListener;
    private PropertyChangeListener cameraNameListener;
    private PropertyChangeListener videoQualityListener;
    private PropertyChangeListener cameraStatusListener;
    private PropertyChangeListener timestampListener;

    public CameraViewModel() {
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
        cameraNameListener = event -> {
            var name = (String) event.getNewValue();
            Platform.runLater(() -> {
                cameraName.set(name);
            });
        };
        videoQualityListener = event -> {
            var quality = (String) event.getNewValue();
            Platform.runLater(() -> {
                videoQuality.set(quality);
            });
        };
        cameraStatusListener = event -> {
            var status = (String) event.getNewValue();
            Platform.runLater(() -> {
                cameraStatus.set(status);
            });
        };
        timestampListener = event -> {
            var time = (String) event.getNewValue();
            Platform.runLater(() -> {
                timestamp.set(time);
            });
        };
    }

    public void init(ViewInfo info) {
        viewInfo = info;
        camera = App.cameraManager().getCamera(viewInfo.cameraUUID);
        drawDetection.set(viewInfo.drawDetection);
        drawStats.set(viewInfo.drawStats);
    }

    @Override
    public void onViewAdded() {
        camera.addPropertyChangeListener("frame", cameraFrameListener);
        camera.addPropertyChangeListener("classifierResults", cameraClassifierListener);
        camera.addPropertyChangeListener("cameraName", cameraNameListener);
        camera.addPropertyChangeListener("videoQuality", videoQualityListener);
        camera.addPropertyChangeListener("cameraStatus", cameraStatusListener);
        camera.addPropertyChangeListener("timestamp", timestampListener);
    }

    @Override
    public void onViewRemoved() {
        camera.removePropertyChangeListener("frame", cameraFrameListener);
        camera.removePropertyChangeListener("classifierResults", cameraClassifierListener);
        camera.removePropertyChangeListener("cameraName", cameraNameListener);
        camera.removePropertyChangeListener("videoQuality", videoQualityListener);
        camera.removePropertyChangeListener("cameraStatus", cameraStatusListener);
        camera.removePropertyChangeListener("timestamp", timestampListener);
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

    public String getCameraName() {
        return cameraName.get();
    }

    public StringProperty cameraNameProperty() {
        return cameraName;
    }

    public String getVideoQuality() {
        return videoQuality.get();
    }

    public StringProperty videoQualityProperty() {
        return videoQuality;
    }

    public String getCameraStatus() {
        return cameraStatus.get();
    }

    public StringProperty cameraStatusProperty() {
        return cameraStatus;
    }

    public String getTimestamp() {
        return timestamp.get();
    }

    public StringProperty timestampProperty() {
        return timestamp;
    }
}
