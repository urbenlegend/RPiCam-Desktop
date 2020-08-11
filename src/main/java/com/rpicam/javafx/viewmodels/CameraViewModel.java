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
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class CameraViewModel implements ViewModel {
    private ViewInfo viewInfo;
    private CameraWorker camera;
    private PropertyChangeListener cameraClassifierListener;
    private PropertyChangeListener cameraFrameListener;

    private SimpleObjectProperty<Image> frame = new SimpleObjectProperty<>();
    private SimpleListProperty<ClassifierResult> classifierResults = new SimpleListProperty<>(FXCollections.observableArrayList());
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
        cameraClassifierListener = event -> {
            var results = (ArrayList<ClassifierResult>) event.getNewValue();
            Platform.runLater(() -> {
                classifierResults.setAll(results);
            });
        };
        cameraFrameListener = event -> {
            var image = (ByteBufferImage) event.getNewValue();
            var jfxImage = wrapByteBufferImage(image);
            Platform.runLater(() -> {
                frame.set(jfxImage);
            });
        };

        camera.addPropertyChangeListener("classifierResults", cameraClassifierListener);
        camera.addPropertyChangeListener("frame", cameraFrameListener);
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
        return new WritableImage(new PixelBuffer<>(image.getWidth(), image.getHeight(), image.getBuffer(), PixelFormat.getByteBgraPreInstance()));
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
