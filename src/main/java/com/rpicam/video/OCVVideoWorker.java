package com.rpicam.video;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import org.opencv.core.Mat;


public class OCVVideoWorker implements VideoWorker {
    private final int QUEUE_SIZE = 60;
    
    private OCVVideoCapture camera;
    private VideoViewModel uiModel;
    private ArrayList<OCVClassifier> classifiers;
    private ArrayBlockingQueue<Mat> imageQueue;
    private ArrayBlockingQueue<Mat> processQueue;
    private ArrayBlockingQueue<ArrayList<ClassifierResult>> classifierResults;
    
    public OCVVideoWorker(OCVVideoCapture cam, VideoViewModel model) {
        camera = cam;
        uiModel = model;
        classifiers = new ArrayList<>();
        imageQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
        processQueue = new ArrayBlockingQueue<>(1);
        classifierResults = new ArrayBlockingQueue<>(QUEUE_SIZE);
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
    
    @Override
    public void grabFrame() {
        try {
            Mat frame = camera.getFrame();
            imageQueue.put(frame);
            
            // Make sure process Queue doesn't fall too far behind by
            // popping off the head of the queue before pushing a new frame
            processQueue.poll();
            processQueue.offer(frame);
        }
        catch (InterruptedException ex) {
            // Okay for thread to be interrupted
        }
        // TODO: Handle exceptions better by using ScheduledFuture
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void processFrame() {
        try {
            Mat frame = processQueue.take();
            ArrayList<ClassifierResult> results = new ArrayList<>();
            for (var c : classifiers) {
                results.addAll(c.apply(frame));
            }
            classifierResults.add(results);
        }
        catch (InterruptedException ex) {
            // Okay for thread to be interrupted
        }
        // TODO: Handle exceptions better by using ScheduledFuture
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    @Override
    public void updateUI() {
        Mat frame = imageQueue.poll();
        if (frame != null) {
            uiModel.setMat(frame);
        }
        ArrayList<ClassifierResult> results = classifierResults.poll();
        if (results != null) {
            uiModel.clearClassifierResults();
            uiModel.addClassifierResults(results);
        }
    }
}
