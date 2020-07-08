package com.rpicam.video;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.rpicam.models.CameraViewModel;
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

public class OCVStreamCamera implements CameraWorker {

    private VideoCapture capture = new VideoCapture();
    private final CameraViewModel viewModel = new CameraViewModel(this);
    private Parameters params = new Parameters();
    private ScheduledExecutorService schedulePool;
    private final UMat capMat = new UMat();
    private final UMat processMat = new UMat();
    private final List<OCVClassifier> classifiers = Collections.synchronizedList(new ArrayList<>());

    private void open() {
        int api;
        try {
            var apiField = opencv_videoio.class.getField(params.captureApi);
            api = apiField.getInt(null);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid camera api specified: " + params.captureApi, ex);
        }

        if (!capture.open(params.url, api)) {
            throw new VideoIOException("Could not open " + params.url);
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

    public void clearClassifiers() {
        classifiers.clear();
    }

    private void capFrameThread() {
        synchronized (capMat) {
            if (!capture.read(capMat)) {
                throw new VideoIOException("could not grab next frame from camera");
            }
            viewModel.updateFrame(capMat);
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

        viewModel.updateClassifierResults(classifierResults);
    }

    @Override
    public CameraViewModel getViewModel() {
        return viewModel;
    }

    @Override
    public String toJson() {
        var builder = new GsonBuilder();
        var gson = builder.create();
        var jsonObj = gson.toJsonTree(params).getAsJsonObject();
        jsonObj.addProperty("type", "path");
        jsonObj.addProperty("drawDetection", viewModel.drawDetectionProperty().get());
        jsonObj.addProperty("drawStats", viewModel.drawStatsProperty().get());
        return jsonObj.toString();
    }

    @Override
    public void fromJson(String jsonStr) {
        var builder = new GsonBuilder();
        var gson = builder.create();
        var jsonObj = JsonParser.parseString(jsonStr).getAsJsonObject();
        var newParams = gson.fromJson(jsonObj, Parameters.class);
        setParameters(newParams);

        boolean drawDetection = jsonObj.get("drawDetection").getAsBoolean();
        boolean drawStats = jsonObj.get("drawStats").getAsBoolean();
        viewModel.drawDetectionProperty().set(drawDetection);
        viewModel.drawStatsProperty().set(drawStats);
    }

    public Parameters getParameters() {
        return params.clone();
    }

    public void setParameters(Parameters newParams) {
        // TODO: Consider automatically stopping and starting camera
        params = newParams;
    }

    public static class Parameters implements Cloneable {
        public String url;
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
