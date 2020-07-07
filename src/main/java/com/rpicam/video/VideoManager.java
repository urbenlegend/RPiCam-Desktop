package com.rpicam.video;

import com.google.gson.GsonBuilder;
import com.rpicam.config.Config;
import com.rpicam.ui.VideoListModel;
import com.rpicam.ui.VideoModel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class VideoManager {

    private VideoListModel model = new VideoListModel(this);
    private ArrayList<VideoWorker> workers = new ArrayList<>();

    public void loadSources(String configPath) throws IOException {
        // TODO: Consider using a less cumbersome JSON library
        var configStr = Files.readString(Paths.get(configPath), StandardCharsets.US_ASCII);
        var builder = new GsonBuilder();
        var gson = builder.create();
        Config config = gson.fromJson(configStr, Config.class);

        var classifiers = new ArrayList<OCVClassifier>();
        for (var classifierConf : config.classifiers) {
            var classifier = new OCVClassifier(classifierConf.path);
            classifier.setTitle(classifierConf.title);
            classifier.setRGB(classifierConf.color);
            classifiers.add(classifier);
        }

        for (var camConf : config.cameras) {
            var cameraWorker = new OCVLocalCamera();

            for (var c : classifiers) {
                cameraWorker.addClassifier(c);
            }

            var camOptions = cameraWorker.getOptions();
            switch (camConf.type) {
                case "local" -> {
                    camOptions.camIndex = camConf.index;
                    camOptions.api = camConf.api;
                    camOptions.resW = camConf.resW;
                    camOptions.resH = camConf.resH;
                    camOptions.capRate = camConf.capRate;
                    camOptions.procRate = camConf.procRate;
                    cameraWorker.setOptions(camOptions);
                    addWorker(cameraWorker);
                }
                // TODO: Add other camera types
            }
        }
    }

    public void startWorkers() {
        for (var w : workers) {
            w.start();
        }
    }

    public void stopWorkers() {
        for (var w : workers) {
            w.stop();
        }
    }

    public void addWorker(VideoWorker worker) {
        workers.add(worker);
        updateModel();
    }

    public void removeWorker(VideoWorker worker) {
        worker.stop();
        workers.remove(worker);
        updateModel();
    }

    public void removeWorkerViaModel(VideoModel model) {
        workers.removeIf((worker) -> worker.getModel() == model);
    }

    public void saveWorkersToJSON() {

    }

    public VideoListModel getModel() {
        return model;
    }

    private void updateModel() {
        var videoList = workers.stream()
                .map(worker -> worker.getModel())
                .collect(Collectors.toList());
        model.updateVideoList(videoList);
    }
}
