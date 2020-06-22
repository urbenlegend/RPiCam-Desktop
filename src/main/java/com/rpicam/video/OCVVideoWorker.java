package com.rpicam.video;

import com.rpicam.exceptions.VideoIOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javafx.animation.AnimationTimer;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_ANY;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_DSHOW;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_HEIGHT;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_WIDTH;
import org.bytedeco.opencv.opencv_core.UMat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;


public class OCVVideoWorker implements VideoWorker {
    private final int QUEUE_SIZE = 15;
    
    private VideoCapture capture;
    private VideoViewModel uiModel;
    private ArrayList<OCVClassifier> classifiers;
    private ArrayBlockingQueue<UMat> imageQueue;
    private ArrayBlockingQueue<UMat> processQueue;
    private ArrayBlockingQueue<ArrayList<ClassifierResult>> classifierResults;
    
    private ScheduledExecutorService schedulePool;
    ScheduledFuture<?> grabThread;
    ScheduledFuture<?> processThread;
    // TODO: Consider removing AnimationTimer out of worker and making updateUI func accept a uiModel
    AnimationTimer drawThread;
    
    public OCVVideoWorker(ScheduledExecutorService pool) {
        capture = new VideoCapture();
        schedulePool = pool;
        classifiers = new ArrayList<>();
        imageQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
        processQueue = new ArrayBlockingQueue<>(1);
        classifierResults = new ArrayBlockingQueue<>(QUEUE_SIZE);
        
        drawThread = new AnimationTimer() {
            @Override
            public void handle(long l) {
                updateUIFunc();
            }
        };
    }
    
    @Override
    public void open(int camIndex, int width, int height) {
        // Don't use OpenCV's MSMF backend on Windows. It is very slow.
        String os = System.getProperty("os.name").toLowerCase();
        int videoAPI = os.contains("win") ? CAP_DSHOW : CAP_ANY;
        
        if (!capture.open(camIndex, videoAPI)) {
            throw new VideoIOException("Could not open camera " + camIndex);
        }
        
        capture.set(CAP_PROP_FRAME_WIDTH, width);
        capture.set(CAP_PROP_FRAME_HEIGHT, height);
    }
    
    @Override
    public void open(String path) {
        if (!capture.open(path)) {
            throw new VideoIOException("Could not open video file " + path);
        }
    }
    
    @Override
    public void close() {
        capture.release();
    }
    
    @Override
    public void start(int grabRate, int processRate) {
        grabThread = schedulePool.scheduleAtFixedRate(this::grabFrameFunc, 0, grabRate, TimeUnit.MILLISECONDS);
        processThread = schedulePool.scheduleAtFixedRate(this::processFrameFunc, 0, processRate, TimeUnit.MILLISECONDS);
        if (uiModel != null) {
            drawThread.start();
        }
    }
    
    @Override
    public void stop() {
        grabThread.cancel(true);
        processThread.cancel(true);
        drawThread.stop();
    }
    
    @Override
    public void bind(VideoViewModel model) {
        drawThread.stop();
        uiModel = model;
        drawThread.start();
    }
    
    @Override
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
    
    private UMat getFrame() {
        UMat frame = new UMat();
        if (!capture.read(frame)) {
            throw new VideoIOException("could not grab next frame from camera");
        }
        return frame;
    }
    
    public void grabFrameFunc() {
        try {
            UMat frame = getFrame();
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
            UMat frame = processQueue.take();
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
        UMat frame = imageQueue.poll();
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
