package com.rpicam.video;

import com.rpicam.dto.video.ClassifierResult;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.rpicam.exceptions.VideoIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.bytedeco.opencv.global.opencv_videoio;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_HEIGHT;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_WIDTH;
import org.bytedeco.opencv.opencv_core.UMat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

public class OCVLocalCamera implements CameraWorker {

    private VideoCapture capture = new VideoCapture();
    private Parameters params = new Parameters();
    private ScheduledExecutorService schedulePool;
    private final UMat capMat = new UMat();
    private final UMat processMat = new UMat();
    private final List<OCVClassifier> classifiers = Collections.synchronizedList(new ArrayList<>());
    private final Map<CameraListener, CameraListener> listeners = Collections.synchronizedMap(new WeakHashMap<>());

    private void open() {
        int api;
        try {
            var apiField = opencv_videoio.class.getField(params.captureApi);
            api = apiField.getInt(null);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid camera api specified: " + params.captureApi, ex);
        }

        if (!capture.open(params.camIndex, api)) {
            throw new VideoIOException("Could not open camera " + params.camIndex);
        }

        capture.set(CAP_PROP_FRAME_WIDTH, params.widthRes);
        capture.set(CAP_PROP_FRAME_HEIGHT, params.heightRes);
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
        schedulePool.scheduleAtFixedRate(this::capFrameThread, 0, params.capRate, TimeUnit.MILLISECONDS);
        schedulePool.scheduleAtFixedRate(this::processFrameThread, 0, params.procRate, TimeUnit.MILLISECONDS);
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

    @Override
    public void addListener(CameraListener listener) {
        // TODO: Remove hacky workaround using a
        // WeakHashMap to implement weak listeners
        listeners.put(listener, listener);
    }

    @Override
    public void addWeakListener(CameraListener listener) {
        listeners.put(listener, null);
    }

    @Override
    public void removeListener(CameraListener listener) {
        listeners.remove(listener);
    }

    private void capFrameThread() {
        synchronized (capMat) {
            if (!capture.read(capMat)) {
                throw new VideoIOException("could not grab next frame from camera");
            }
            listeners.forEach((listener, dummy) -> {
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

        listeners.forEach((listener, dummy) -> {
            listener.onClassifierResults(classifierResults);
        });
    }

    @Override
    public String toJson() {
        var builder = new GsonBuilder();
        var gson = builder.create();
        var jsonObj = gson.toJsonTree(params).getAsJsonObject();
        jsonObj.addProperty("type", "local");
        return jsonObj.toString();
    }

    @Override
    public void fromJson(String jsonStr) {
        var builder = new GsonBuilder();
        var gson = builder.create();
        var jsonObj = JsonParser.parseString(jsonStr).getAsJsonObject();
        var newParams = gson.fromJson(jsonObj, Parameters.class);
        setParameters(newParams);
    }

    public Parameters getParameters() {
        return params.clone();
    }

    public void setParameters(Parameters newParams) {
        // TODO: Consider automatically stopping and starting camera
        params = newParams;
    }

    public static class Parameters implements Cloneable {

        public int camIndex;
        public String captureApi;
        public int widthRes;
        public int heightRes;
        public int capRate;
        public int procRate;

        @Override
        public Parameters clone() {
            try {return (Parameters) super.clone();}
            catch (CloneNotSupportedException ex) {return null;}
        }
    }
}
