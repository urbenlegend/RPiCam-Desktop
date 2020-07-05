package com.rpicam.video;

import com.rpicam.ui.VideoViewModel;
import com.rpicam.exceptions.VideoIOException;
import com.rpicam.util.VideoUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2BGRA;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import org.bytedeco.opencv.global.opencv_videoio;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_HEIGHT;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_WIDTH;
import org.bytedeco.opencv.opencv_core.UMat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;


public class OCVVideoWorker implements VideoWorker {
    private VideoCapture capture = new VideoCapture();
    private final UMat capMat = new UMat();
    private final UMat processMat = new UMat();
    private final UMat bgraMat = new UMat();
    private final List<OCVClassifier> classifiers = Collections.synchronizedList(new ArrayList<>());
    private final List<VideoViewModel> videoModels = Collections.synchronizedList(new ArrayList<>());;
    private ScheduledExecutorService schedulePool;
    private Options options = new Options();

    public static class Options implements Cloneable {
        public String type;
        public int camIndex;
        public String api;
        public int resW;
        public int resH;
        public String path;
        public int grabRate;
        public int processRate;

        @Override
        public Options clone() throws CloneNotSupportedException {
            return (Options) super.clone();
        }
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

        switch (options.type) {
            case "local":
                if (!capture.open(options.camIndex, api)) {
                    throw new VideoIOException("Could not open camera " + options.camIndex);
                }

                capture.set(CAP_PROP_FRAME_WIDTH, options.resW);
                capture.set(CAP_PROP_FRAME_HEIGHT, options.resH);
                break;
            case "url":
                if (!capture.open(options.path)) {
                    throw new VideoIOException("Could not open video file " + options.path);
                }
                break;
        }
    }

    private void close() {
        capture.release();
    }

    @Override
    public void start() {
        if (schedulePool != null || capture.isOpened()) {
            return;
        }
        open();
        schedulePool = Executors.newScheduledThreadPool(2);
        schedulePool.scheduleAtFixedRate(this::grabFrameThread, 0, options.grabRate, TimeUnit.MILLISECONDS);
        schedulePool.scheduleAtFixedRate(this::processFrameThread, 0, options.processRate, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        schedulePool.shutdownNow();
        schedulePool = null;
        close();
    }

    @Override
    public void bindModel(VideoViewModel model) {
        videoModels.add(model);
    }

    @Override
    public void unbindModel(VideoViewModel model) {
        videoModels.remove(model);
    }

    public void addClassifier(OCVClassifier c) {
        classifiers.add(c);
    }

    public void removeClassifier(OCVClassifier c) {
        classifiers.remove(c);
    }

    public void clearClassifiers() {
        classifiers.clear();
    }

    private void grabFrameThread() {
        synchronized (capMat) {
            if (!capture.read(capMat)) {
                throw new VideoIOException("could not grab next frame from camera");
            }
            synchronized (bgraMat) {
                cvtColor(capMat, bgraMat, COLOR_BGR2BGRA);
            }
        }

        Platform.runLater(() -> {
            synchronized (bgraMat) {
                synchronized(videoModels) {
                    for (var model : videoModels) {
                        model.frameProperty().set(VideoUtils.wrapBgraUMat(bgraMat));
                    }
                }
            }
        });
    }

    private void processFrameThread() {
        synchronized (capMat) {
            capMat.copyTo(processMat);
        }

        var classifierResults = new ArrayList<ClassifierResult>();
        classifiers.forEach(c -> {
            classifierResults.addAll(c.apply(processMat));
        });

        Platform.runLater(() -> {
            synchronized (videoModels) {
                for (var model : videoModels) {
                    model.clearClassifierResults();
                    model.addClassifierResults(classifierResults);
                }
            }
        });
    }
}
