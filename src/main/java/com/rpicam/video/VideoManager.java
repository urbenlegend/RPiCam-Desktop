package com.rpicam.video;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.json.JSONObject;

public class VideoManager {
    private static VideoManager managerSingleton;
    
    public static VideoManager getInstance() {
        if (managerSingleton == null) {
            managerSingleton = new VideoManager();
        }
        return managerSingleton;
    }
    
    private ScheduledExecutorService schedulePool;
    private HashMap<UUID, VideoWorker> workers;
    
    public VideoManager() {
        schedulePool = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        workers = new HashMap<>();
    }
    
    public void loadSources(String configPath) throws IOException {
        // TODO: Consider using a less cumbersome JSON library
        var configStr = Files.readString(Paths.get(configPath), StandardCharsets.US_ASCII);
        var configJSON = new JSONObject(configStr);
        
        var classifiers = new ArrayList<OCVClassifier>();
        var classifiersJSON = configJSON.getJSONArray("classifiers");
        for (int i = 0; i < classifiersJSON.length(); i++) {
            var classifierObj = classifiersJSON.getJSONObject(i);
            var title = classifierObj.getString("title");
            var r = classifierObj.getJSONObject("color").getInt("r");
            var g = classifierObj.getJSONObject("color").getInt("g");
            var b = classifierObj.getJSONObject("color").getInt("b");
            var path = classifierObj.getString("path");
            
            var classifier = new OCVClassifier(path);
            classifier.setTitle(title);
            classifier.setRGB(r, g, b);
            classifiers.add(classifier);
        }
        
        var camsJSON = configJSON.getJSONArray("cameras");
        for (int i = 0; i < camsJSON.length(); i++) {
            var camObj = camsJSON.getJSONObject(i);
            var uuid = UUID.fromString(camObj.getString("uuid"));
            var index = camObj.getInt("index");
            var resW = camObj.getJSONObject("res").getInt("w");
            var resH = camObj.getJSONObject("res").getInt("h");
            var cap_rate = camObj.getInt("cap_rate");
            var proc_rate = camObj.getInt("proc_rate");
            
            var cameraWorker = new OCVVideoWorker(schedulePool);
            
            for (var c : classifiers) {
                cameraWorker.addClassifier(c);
            }
            
            cameraWorker.open(index, resW, resH);
            cameraWorker.start(cap_rate, proc_rate);
            addWorker(cameraWorker, uuid);
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
        worker.close();
        workers.remove(workerUUID);
    }
    
    public HashMap<UUID, VideoWorker> getWorkers() {
        return workers;
    }
    
    public void stopWorkers() {
        for (var w : workers.values()) {
            w.stop();
            w.close();
        }
        schedulePool.shutdownNow();
    }
    
    public void saveWorkersToJSON() {
        
    }
}