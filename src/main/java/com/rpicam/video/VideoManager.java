package com.rpicam.video;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
    
    public void loadSources(String configFile) {
        // TODO: Actually load from JSON file
        var upperBodyModel = new OCVClassifier("./data/upperbody_recognition_model.xml");
        upperBodyModel.setTitle("Upper Body");
        upperBodyModel.setRGB(255, 0, 0);
        var facialModel = new OCVClassifier("./data/facial_recognition_model.xml");
        facialModel.setTitle("Face");
        facialModel.setRGB(0, 255, 0);
        var fullBodyModel = new OCVClassifier("./data/fullbody_recognition_model.xml");
        fullBodyModel.setTitle("Full Body");
        fullBodyModel.setRGB(0, 0, 255);

        
        var cameraWorker = new OCVVideoWorker(schedulePool);
        cameraWorker.addClassifier(upperBodyModel);
        cameraWorker.addClassifier(facialModel);
        cameraWorker.addClassifier(fullBodyModel);
        cameraWorker.open(0, 1920, 1080);
        cameraWorker.start(16, 80);
        addWorker(cameraWorker, UUID.fromString("dd243140-b03a-4d72-b5ce-8f31412af8a5"));
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
    
    public VideoWorker getWorker(UUID workerUUID) {
        return workers.get(workerUUID);
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