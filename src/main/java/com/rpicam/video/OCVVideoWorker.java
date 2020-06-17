package com.rpicam.video;

import com.rpicam.exceptions.VideoIOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.animation.AnimationTimer;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;


public class OCVVideoWorker {
    private final int QUEUE_SIZE = 60;
    
    private VideoCapture capture;
    private VideoViewModel uiModel;
    private ArrayList<OCVClassifier> classifiers;
    private ArrayBlockingQueue<Mat> imageQueue;
    private ArrayBlockingQueue<Mat> processQueue;
    private ArrayBlockingQueue<ArrayList<ClassifierResult>> classifierResults;
    
    private ScheduledExecutorService schedulePool;
    ScheduledFuture<?> grabThread;
    ScheduledFuture<?> processThread;
    AnimationTimer drawThread;
    
    public OCVVideoWorker(ScheduledExecutorService pool) {
        capture = new VideoCapture();
        schedulePool = pool;
        classifiers = new ArrayList<>();
        imageQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
        processQueue = new ArrayBlockingQueue<>(1);
        classifierResults = new ArrayBlockingQueue<>(QUEUE_SIZE);
    }
    
    public void open(int camIndex) {
        // Detect OS and use the right camera API
        // Necessary because CAP_ANY is too slow, but it is used for fallback
        String os = System.getProperty("os.name").toLowerCase();
        int videoAPI = Videoio.CAP_ANY;
        if (os.contains("win")) {
            videoAPI = Videoio.CAP_DSHOW;
        }
        else if (os.contains("mac")) {
            videoAPI = Videoio.CAP_AVFOUNDATION;
        }
        else if (os.contains("linux")) {
            videoAPI = Videoio.CAP_V4L2;
        }
        
        if (!capture.open(camIndex, videoAPI)) {
            throw new VideoIOException("Could not open camera " + camIndex);
        }
    }
    
    public void open(String filename) {
        if (!capture.open(filename)) {
            throw new VideoIOException("Could not open video file " + filename);
        }
    }
    
    public void close() {
        capture.release();
    }
    
    public void start(int grabRate, int processRate) {
        grabThread = schedulePool.scheduleAtFixedRate(this::grabFrameFunc, 0, grabRate, TimeUnit.MILLISECONDS);
        processThread = schedulePool.scheduleAtFixedRate(this::processFrameFunc, 0, processRate, TimeUnit.MILLISECONDS);
        
        // Draw loop
        drawThread = new AnimationTimer() {
            @Override
            public void handle(long l) {
                updateUIFunc();
            }
        };
    }
    
    public void stop() {
        grabThread.cancel(true);
        processThread.cancel(true);
        drawThread.stop();
    }
    
    public void bindTo(VideoViewModel model) {
        uiModel = model;
        drawThread.start();
    }
    
    public void unbind() {
        drawThread.stop();
        uiModel = null;
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
    
    private Mat getFrame() {
        Mat frame = new Mat();
        if (!capture.read(frame)) {
            throw new VideoIOException("could not grab next frame from camera");
        }
        return frame;
    }
    
    public void grabFrameFunc() {
        try {
            Mat frame = getFrame();
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

    public void processFrameFunc() {
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
    
    public void updateUIFunc() {
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
