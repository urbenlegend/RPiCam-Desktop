package com.rpicam.video;

import com.rpicam.detection.ClassifierResult;
import com.rpicam.detection.OCVClassifier;
import com.rpicam.config.OCVCameraConfig;
import com.rpicam.config.OCVStreamCameraConfig;
import com.rpicam.exceptions.ConfigException;
import com.rpicam.exceptions.VideoIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.bytedeco.opencv.global.opencv_videoio;
import org.bytedeco.opencv.opencv_core.UMat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

public class OCVStreamCamera extends CameraWorker {
    private VideoCapture capture = new VideoCapture();
    private String url;
    private String captureApi;
    private int widthRes;
    private int heightRes;
    private int capRate;
    private int procRate;

    private ScheduledExecutorService schedulePool;
    private final UMat capMat = new UMat();
    private final UMat processMat = new UMat();
    private final List<OCVClassifier> classifiers = Collections.synchronizedList(new ArrayList<>());

    @Override
    public OCVStreamCameraConfig toConfig() {
        var conf = new OCVStreamCameraConfig();
        conf.url = url;
        conf.captureApi = captureApi;
        conf.widthRes = widthRes;
        conf.heightRes = heightRes;
        conf.capRate = capRate;
        conf.procRate = procRate;

        return conf;
    }

    @Override
    public void fromConfig(OCVCameraConfig conf) {
        if (!(conf instanceof OCVStreamCameraConfig)) {
            throw new ConfigException("Invalid config for OCVLocalCamera");
        }

        var localConf = (OCVStreamCameraConfig) conf;

        url = localConf.url;
        captureApi = localConf.captureApi;
        widthRes = localConf.widthRes;
        heightRes = localConf.heightRes;
        capRate = localConf.capRate;
        procRate = localConf.procRate;
    }

    private void open() {
        int api;
        try {
            var apiField = opencv_videoio.class.getField(captureApi);
            api = apiField.getInt(null);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid camera api specified: " + captureApi, ex);
        }

        if (!capture.open(url, api)) {
            throw new VideoIOException("Could not open " + url);
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
        schedulePool.scheduleAtFixedRate(this::capFrameThread, 0, capRate, TimeUnit.MILLISECONDS);
        schedulePool.scheduleAtFixedRate(this::processFrameThread, 0, procRate, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        schedulePool.shutdownNow();
        schedulePool = null;
        close();
    }

    public void addClassifier(OCVClassifier c) {
        classifiers.add(c);
    }

    public void removeClassifier(OCVClassifier c) {
        classifiers.remove(c);
    }

    private void capFrameThread() {
        synchronized (capMat) {
            if (!capture.read(capMat)) {
                throw new VideoIOException("could not grab next frame from camera");
            }
            getListeners().forEach((listener) -> {
                listener.onFrame(capMat);
            });
        }
    }

    private void processFrameThread() {
        synchronized (capMat) {
            capMat.copyTo(processMat);
        }

        var classifierResults = new ArrayList<ClassifierResult>();
        classifiers.forEach(c -> {
            classifierResults.addAll(c.apply(processMat));
        });

        getListeners().forEach((listener) -> {
            listener.onClassifierResults(classifierResults);
        });
    }
}
