package com.rpicam.video;

import com.rpicam.ui.VideoModel;
import com.rpicam.exceptions.VideoIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.bytedeco.opencv.global.opencv_videoio;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_HEIGHT;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_WIDTH;
import org.bytedeco.opencv.opencv_core.UMat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

public class OCVLocalCamera implements VideoWorker {

    private final UMat capMat = new UMat();
    private VideoCapture capture = new VideoCapture();
    private final List<OCVClassifier> classifiers = Collections.synchronizedList(new ArrayList<>());
    private final VideoModel model = new VideoModel(this);
    private Options options = new Options();
    private final UMat processMat = new UMat();
    private ScheduledExecutorService schedulePool;

    public void addClassifier(OCVClassifier c) {
        classifiers.add(c);
    }

    private void capFrameThread() {
        synchronized (capMat) {
            if (!capture.read(capMat)) {
                throw new VideoIOException("could not grab next frame from camera");
            }
            model.updateFrameLater(capMat);
        }
    }

    public void clearClassifiers() {
        classifiers.clear();
    }

    private void close() {
        capture.release();
    }

    @Override
    public VideoModel getModel() {
        return model;
    }

    public Options getOptions() {
        try {
            return options.clone();
        } catch (CloneNotSupportedException ex) {
            return null;
        }
    }

    public void setOptions(Options newOptions) {
        // TODO: Consider automatically stopping and starting camera
        options = newOptions;
    }

    private void open() {
        int api;
        try {
            var apiField = opencv_videoio.class.getField(options.api);
            api = apiField.getInt(null);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid camera api specified: " + options.api, ex);
        }

        if (!capture.open(options.camIndex, api)) {
            throw new VideoIOException("Could not open camera " + options.camIndex);
        }

        capture.set(CAP_PROP_FRAME_WIDTH, options.resW);
        capture.set(CAP_PROP_FRAME_HEIGHT, options.resH);
    }

    private void processFrameThread() {
        synchronized (capMat) {
            capMat.copyTo(processMat);
        }

        var classifierResults = new ArrayList<ClassifierResult>();
        classifiers.forEach(c -> {
            classifierResults.addAll(c.apply(processMat));
        });

        model.updateClassifierResultsLater(classifierResults);
    }

    public void removeClassifier(OCVClassifier c) {
        classifiers.remove(c);
    }

    @Override
    public void start() {
        if (schedulePool != null || capture.isOpened()) {
            return;
        }
        open();
        schedulePool = Executors.newScheduledThreadPool(2);
        schedulePool.scheduleAtFixedRate(this::capFrameThread, 0, options.capRate, TimeUnit.MILLISECONDS);
        schedulePool.scheduleAtFixedRate(this::processFrameThread, 0, options.procRate, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        schedulePool.shutdownNow();
        schedulePool = null;
        close();
    }

    public static class Options implements Cloneable {

        public int camIndex;
        public String api;
        public int resW;
        public int resH;
        public int capRate;
        public int procRate;

        @Override
        public Options clone() throws CloneNotSupportedException {
            return (Options) super.clone();
        }
    }
}
