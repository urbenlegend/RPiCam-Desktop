package com.rpicam.video;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.animation.AnimationTimer;

public class VideoManager {
    private ScheduledExecutorService schedulePool;
    private HashMap<VideoWorker, VideoContract> contracts;
    
    private class VideoContract {
        ScheduledFuture<?> grabThread;
        ScheduledFuture<?> processThread;
        AnimationTimer drawThread;
    }
    
    public VideoManager() {
        schedulePool = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        contracts = new HashMap<>();
    }
    
    public void addWorker(VideoWorker worker, int grabRate, int processRate) {
        var contract = new VideoContract();
        contract.grabThread = schedulePool.scheduleAtFixedRate(worker::grabFrame, 0, grabRate, TimeUnit.MILLISECONDS);
        contract.processThread = schedulePool.scheduleAtFixedRate(worker::processFrame, 0, processRate, TimeUnit.MILLISECONDS);
        
        // Draw loop
        contract.drawThread = new AnimationTimer() {
            @Override
            public void handle(long l) {
                worker.updateUI();
            }
        };
        contract.drawThread.start();
        
        contracts.put(worker, contract);
    }
    
    public void removeWorker(VideoWorker worker) {
        VideoContract contract = contracts.get(worker);
        contract.grabThread.cancel(true);
        contract.processThread.cancel(true);
        contract.drawThread.stop();
        contracts.remove(worker);
    }
    
    public void stopWorkers() {
        schedulePool.shutdownNow();
        for (var c : contracts.values()) {
            c.drawThread.stop();
        }
    }
}