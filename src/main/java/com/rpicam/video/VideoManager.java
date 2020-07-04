package com.rpicam.video;

import com.google.gson.GsonBuilder;
import com.rpicam.config.ConfigGSON;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class VideoManager {
    private HashMap<UUID, VideoWorker> workers = new HashMap<>();
    private static VideoManager managerSingleton;

    public static VideoManager getInstance() {
        if (managerSingleton == null) {
            managerSingleton = new VideoManager();
        }
        return managerSingleton;
    }

    public static void deleteInstance() {
        if (managerSingleton != null) {
            managerSingleton.stopWorkers();
            managerSingleton = null;
        }
    }

    public void loadSources(String configPath) throws IOException {
        // TODO: Consider using a less cumbersome JSON library
        var configStr = Files.readString(Paths.get(configPath), StandardCharsets.US_ASCII);
        var builder = new GsonBuilder();
        var gson = builder.create();
        ConfigGSON config = gson.fromJson(configStr, ConfigGSON.class);

        var classifiers = new ArrayList<OCVClassifier>();
        for (var classifierGSON : config.classifiers) {
            var classifier = new OCVClassifier(classifierGSON.path);
            classifier.setTitle(classifierGSON.title);
            classifier.setRGB(classifierGSON.color);
            classifiers.add(classifier);
        }

        for (var camGSON : config.cameras) {
            var cameraWorker = new OCVVideoWorker();

            for (var c : classifiers) {
                cameraWorker.addClassifier(c);
            }

            var camOptions = cameraWorker.getOptions();
            camOptions.type = camGSON.type;
            camOptions.camIndex = camGSON.index;
            camOptions.api = camGSON.api;
            camOptions.resW = camGSON.resW;
            camOptions.resH = camGSON.resH;
            camOptions.grabRate = camGSON.capRate;
            camOptions.processRate = camGSON.procRate;
            cameraWorker.setOptions(camOptions);
            cameraWorker.start();
            addWorker(cameraWorker, UUID.fromString(camGSON.uuid));
        }
    }

    public void addWorker(VideoWorker worker, UUID workerUUID) {
        if (workerUUID == null) {
            workerUUID = UUID.randomUUID();
        }
        workers.put(workerUUID, worker);
    }

    public void removeWorker(UUID workerUUID) {
        var worker = workers.get(workerUUID);
        worker.stop();
        workers.remove(workerUUID);
    }

    public VideoWorker getWorker(UUID workerUUID) {
        return workers.get(workerUUID);
    }

    public HashMap<UUID, VideoWorker> getWorkers() {
        return workers;
    }

    public void stopWorkers() {
        for (var w : workers.values()) {
            w.stop();
        }
    }

    public void saveWorkersToJSON() {

    }
}