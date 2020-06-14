/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.video;

import com.rpicam.ui.VideoViewModel;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import org.opencv.core.Mat;


/**
 *
 * @author benrx
 */
public class VideoWorker {
    private final int QUEUE_SIZE = 60;
    
    private OCVVideoCapture camera;
    private VideoViewModel uiModel;
    private ArrayList<OCVClassifier> classifiers;
    private ArrayBlockingQueue<Mat> imageQueue;
    private ArrayBlockingQueue<ArrayList<ClassifierResult>> classifierResults;
    
    public VideoWorker(OCVVideoCapture cam, VideoViewModel model) {
        camera = cam;
        uiModel = model;
        classifiers = new ArrayList<>();
        imageQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
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

    public void processFrame() {
        try {
            Mat frame = camera.getFrame();
            imageQueue.put(frame);
            // TODO: For smoother playback consider splitting classification into a separate thread.
            ArrayList<ClassifierResult> results = new ArrayList<>();
            for (var c : classifiers) {
                results.addAll(c.apply(frame));
            }
            classifierResults.add(results);
        }
        catch (InterruptedException ex) {
            // Okay for thread to be interrupted
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public void sendToModel() {
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
