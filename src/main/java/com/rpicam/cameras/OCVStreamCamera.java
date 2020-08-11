package com.rpicam.cameras;

import com.rpicam.detection.ClassifierResult;
import com.rpicam.detection.OCVClassifier;
import com.rpicam.config.OCVCameraConfig;
import com.rpicam.config.OCVStreamCameraConfig;
import com.rpicam.exceptions.ConfigException;
import com.rpicam.exceptions.VideoIOException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2BGRA;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import org.bytedeco.opencv.global.opencv_videoio;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

public class OCVStreamCamera implements CameraWorker {
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private VideoCapture capture = new VideoCapture();
    private String url;
    private String captureApi;
    private int capRate;
    private int procRate;
    private int procCount = 0;

    private ScheduledExecutorService schedulePool;
    private final Mat capMat = new Mat();
    private final Mat bgraMat = new Mat();
    private final List<OCVClassifier> classifiers = Collections.synchronizedList(new ArrayList<>());

    @Override
    public OCVStreamCameraConfig toConfig() {
        var conf = new OCVStreamCameraConfig();
        conf.url = url;
        conf.captureApi = captureApi;
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
        schedulePool = Executors.newScheduledThreadPool(1);
        schedulePool.scheduleAtFixedRate(this::processFrame, 0, capRate, TimeUnit.MILLISECONDS);
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

    private void processFrame() {
        if (!capture.read(capMat)) {
            throw new VideoIOException("could not grab next frame from camera");
        }

        cvtColor(capMat, bgraMat, COLOR_BGR2BGRA);
        pcs.firePropertyChange("frame", null, new ByteBufferImage(bgraMat.createBuffer(), bgraMat.cols(), bgraMat.rows()));

        if (procCount % procRate == 0) {
            var classifierResults = new ArrayList<ClassifierResult>();
            classifiers.forEach(c -> {
                classifierResults.addAll(c.apply(capMat));
            });

            pcs.firePropertyChange("classifierResults", null, classifierResults);
        }

        procCount++;
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }
}
