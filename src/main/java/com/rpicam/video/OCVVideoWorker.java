package com.rpicam.video;

import com.rpicam.util.VideoUtils;
import com.rpicam.util.MemoryPool;
import com.rpicam.exceptions.VideoIOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.animation.AnimationTimer;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2BGRA;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_ANY;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_DSHOW;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_HEIGHT;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_WIDTH;
import org.bytedeco.opencv.opencv_core.UMat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;


public class OCVVideoWorker implements VideoWorker {
    private final int QUEUE_SIZE = 2;
    
    private VideoCapture capture = new VideoCapture();
    private MemoryPool<UMat> capturePool;
    // Allocate a bgra mat for UI display purposes
    private UMat bgraMat = new UMat();
    
    private ArrayBlockingQueue<UMat> imageQueue = new ArrayBlockingQueue<>(QUEUE_SIZE);
    private ArrayBlockingQueue<UMat> processQueue = new ArrayBlockingQueue<>(1);
    private ArrayBlockingQueue<ArrayList<ClassifierResult>> classifierResults = new ArrayBlockingQueue<>(QUEUE_SIZE);
    
    private ArrayList<OCVClassifier> classifiers = new ArrayList<>();
    private VideoViewModel uiModel;
    
    private ScheduledExecutorService schedulePool;
    // TODO: Consider removing AnimationTimer out of worker and making updateUI func accept a uiModel
    AnimationTimer drawThread;
    
    public OCVVideoWorker() {
        capturePool = new MemoryPool<>(QUEUE_SIZE, () -> {return new UMat();});
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
        if (schedulePool != null) {
            return;
        }
        schedulePool = Executors.newScheduledThreadPool(2);
        schedulePool.scheduleAtFixedRate(this::grabFrameFunc, 0, grabRate, TimeUnit.MILLISECONDS);
        schedulePool.scheduleAtFixedRate(this::processFrameFunc, 0, processRate, TimeUnit.MILLISECONDS);
        if (uiModel != null) {
            drawThread.start();
        }
    }
    
    @Override
    public void stop() {
        schedulePool.shutdownNow();
        schedulePool = null;
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
    
    private UMat getFrame() throws InterruptedException {
        UMat frame = capturePool.get();
        if (!capture.read(frame)) {
            throw new VideoIOException("could not grab next frame from camera");
        }
        return frame;
    }
    
    private void grabFrameFunc() {
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

    private void processFrameFunc() {
        try {
            UMat frame = processQueue.take();
            ArrayList<ClassifierResult> results = new ArrayList<>();
            for (var c : classifiers) {
                results.addAll(c.apply(frame));
            }
            classifierResults.put(results);
        }
        catch (InterruptedException ex) {
            // Okay for thread to be interrupted
        }
        // TODO: Handle exceptions better by using ScheduledFuture
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void updateUIFunc() {
        UMat frame = imageQueue.poll();
        if (frame != null) {
            cvtColor(frame, bgraMat, COLOR_BGR2BGRA);
            try {
                capturePool.free(frame);
            }
            catch (InterruptedException ex) {
                // Safe to ignore
            }
            uiModel.frameProperty().set(VideoUtils.wrapBgraUMat(bgraMat));
        }
        ArrayList<ClassifierResult> results = classifierResults.poll();
        if (results != null) {
            uiModel.clearClassifierResults();
            uiModel.addClassifierResults(results);
        }
    }
}
