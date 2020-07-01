package com.rpicam.video;

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
import static org.bytedeco.opencv.global.opencv_videoio.CAP_ANY;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_DSHOW;
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
    private final List<ClassifierResult> classifierResults = Collections.synchronizedList(new ArrayList<>());
    private final List<VideoViewModel> uiModels = Collections.synchronizedList(new ArrayList<>());;
    private ScheduledExecutorService schedulePool;

    @Override
    public void open(int camIndex, int width, int height) {
        // Don't use OpenCV's MSMF backend on Windows. It is very slow.
        String os = System.getProperty("os.name").toLowerCase();
        int videoAPI = os.contains("win") ? CAP_DSHOW : CAP_ANY;

        if (!capture.open(camIndex, videoAPI)) {
            throw new VideoIOException("Could not open camera " + camIndex);
        }

        capture.set(CAP_PROP_FRAME_WIDTH, width);
        capture.set(CAP_PROP_FRAME_HEIGHT, height);
    }

    @Override
    public void open(String path) {
        if (!capture.open(path)) {
            throw new VideoIOException("Could not open video file " + path);
        }
    }

    @Override
    public void close() {
        capture.release();
    }

    @Override
    public void start(int grabRate, int processRate) {
        if (schedulePool != null) {
            return;
        }
        schedulePool = Executors.newScheduledThreadPool(2);
        schedulePool.scheduleAtFixedRate(this::grabFrameThread, 0, grabRate, TimeUnit.MILLISECONDS);
        schedulePool.scheduleAtFixedRate(this::processFrameThread, 0, processRate, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        schedulePool.shutdownNow();
        schedulePool = null;
    }

    @Override
    public List<VideoViewModel> getModels() {
        return uiModels;
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
                synchronized(uiModels) {
                    for (var model : uiModels) {
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

        classifierResults.clear();
        classifiers.forEach(c -> {
            classifierResults.addAll(c.apply(processMat));
        });

        Platform.runLater(() -> {
            synchronized (uiModels) {
                for (var model : uiModels) {
                    model.clearClassifierResults();
                    model.addClassifierResults(classifierResults);
                }
            }
        });
    }
}
