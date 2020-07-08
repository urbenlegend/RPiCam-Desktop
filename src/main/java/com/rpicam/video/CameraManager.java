package com.rpicam.video;

import com.google.gson.GsonBuilder;
import com.rpicam.config.CameraConfig;
import com.rpicam.config.ClassifierConfig;
import com.rpicam.config.Config;
import com.rpicam.models.CameraManagerModel;
import com.rpicam.models.CameraModel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class CameraManager {

    private CameraManagerModel model = new CameraManagerModel(this);
    private ArrayList<CameraWorker> workers = new ArrayList<>();
    private ArrayList<OCVClassifier> classifiers = new ArrayList<>();

    public void loadConfig(String configPath) throws IOException {
        // TODO: Consider using a less cumbersome JSON library
        var configStr = Files.readString(Paths.get(configPath), StandardCharsets.US_ASCII);
        var builder = new GsonBuilder();
        var gson = builder.create();
        Config config = gson.fromJson(configStr, Config.class);

        for (var classifierConf : config.classifiers) {
            var classifier = new OCVClassifier(classifierConf.path);
            classifier.setConfig(classifierConf);
            classifiers.add(classifier);
        }

        for (var camConf : config.cameras) {
            switch (camConf.type) {
                case "local" -> {
                    var worker = new OCVLocalCamera();
                    worker.setConfig(camConf);
                    addWorker(worker);
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

    public void addWorker(CameraWorker worker) {
        if (worker instanceof OCVLocalCamera) {
            var ocvWorker = (OCVLocalCamera) worker;
            for (var c : classifiers) {
                ocvWorker.addClassifier(c);
            }
        }

        workers.add(worker);
        updateModel();
    }

    public void removeWorker(CameraWorker worker) {
        worker.stop();
        workers.remove(worker);
        updateModel();
    }

    public void removeWorkerViaModel(CameraModel model) {
        workers.removeIf((worker) -> worker.getModel() == model);
    }

    public void saveConfig(String configPath) throws IOException {
        var config = new Config();
        config.classifiers = new ClassifierConfig[classifiers.size()];
        config.cameras = new CameraConfig[workers.size()];
        for (int i = 0; i < config.classifiers.length; i++) {
            config.classifiers[i] = classifiers.get(i).getConfig();
        }
        for (int i = 0; i < config.cameras.length; i++) {
            config.cameras[i] = workers.get(i).getConfig();
        }

        var builder = new GsonBuilder();
        var gson = builder.setPrettyPrinting().create();
        String configJSON = gson.toJson(config, Config.class);
        Files.writeString(Paths.get(configPath), configJSON);
    }

    public CameraManagerModel getModel() {
        return model;
    }

    private void updateModel() {
        var videoList = workers.stream()
                .map(worker -> worker.getModel())
                .collect(Collectors.toList());
        model.updateCameraList(videoList);
    }
}
