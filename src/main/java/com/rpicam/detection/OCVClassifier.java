package com.rpicam.detection;

import com.rpicam.cameras.ByteBufferImage;
import com.rpicam.config.OCVClassifierConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.global.opencv_core;
import static org.bytedeco.opencv.global.opencv_core.CV_8UC3;
import static org.bytedeco.opencv.global.opencv_core.CV_8UC4;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGRA2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_objdetect.CASCADE_SCALE_IMAGE;
import org.bytedeco.opencv.opencv_core.GpuMat;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.UMat;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

public class OCVClassifier implements Classifier, Cloneable {
    private Mat grayMat = new Mat();
    private UMat gpuMat;
    private GpuMat cudaMat;
    private CascadeClassifier classifier;
    private String path = "";
    private String color = "";
    private String title = "";
    private double scaleFactor;
    private int minNeighbors;
    private float minSizeFactor;

    public OCVClassifier(String path, String title, String color, double scaleFactor, int minNeighbors, float minSizeFactor, boolean gpu) {
        this.path = path;
        this.title = title;
        this.color = color;
        this.scaleFactor = scaleFactor;
        this.minNeighbors = minNeighbors;
        this.minSizeFactor = minSizeFactor;

        initClassifier();

        // Allocate GPU memory if gpu flag is enabled
        if (gpu) {
            if (opencv_core.useOpenCL()) {
                gpuMat = new UMat();
            }
            else {
                cudaMat = new GpuMat();
            }
        }
    }

    private void initClassifier() {
        classifier = new CascadeClassifier();
        classifier.load(path);
    }

    @Override
    public List<ClassifierResult> apply(ByteBufferImage image) {
        // Convert frame to grayscale for better detection and efficiency
        switch (image.getFormat()) {
            case BGR -> {
                var imageMat = new Mat(image.getHeight(), image.getWidth(), CV_8UC3, new BytePointer(image.getBuffer()));
                cvtColor(imageMat, grayMat, COLOR_BGR2GRAY);
            }
            case BGRA -> {
                var imageMat = new Mat(image.getHeight(), image.getWidth(), CV_8UC4, new BytePointer(image.getBuffer()));
                cvtColor(imageMat, grayMat, COLOR_BGRA2GRAY);
            }
            default -> {
                throw new IllegalArgumentException("Only BGR and BGRA images are supported");
            }
        }

        int minSize = Math.round(grayMat.rows() * minSizeFactor);
        var detectedObjs = new RectVector();
        if (gpuMat != null) {
            grayMat.copyTo(gpuMat);
            classifier.detectMultiScale(gpuMat,
                    detectedObjs,
                    scaleFactor,
                    minNeighbors,
                    CASCADE_SCALE_IMAGE,
                    new Size(minSize, minSize),
                    new Size()
            );
        }
        else if (cudaMat != null) {
            grayMat.copyTo(cudaMat);
            classifier.detectMultiScale(cudaMat,
                    detectedObjs,
                    scaleFactor,
                    minNeighbors,
                    CASCADE_SCALE_IMAGE,
                    new Size(minSize, minSize),
                    new Size()
            );
        }
        else {
            classifier.detectMultiScale(grayMat,
                    detectedObjs,
                    scaleFactor,
                    minNeighbors,
                    CASCADE_SCALE_IMAGE,
                    new Size(minSize, minSize),
                    new Size()
            );
        }

        var results = new ArrayList<ClassifierResult>();
        for (var obj : detectedObjs.get()) {
            results.add(new ClassifierResult(obj.x(), obj.y(), obj.width(), obj.height(), title, color));
        }

        return results;
    }

    @Override
    public OCVClassifierConfig toConfig() {
        var conf = new OCVClassifierConfig();
        conf.path = path;
        conf.title = title;
        conf.color = color;
        conf.scaleFactor = scaleFactor;
        conf.minNeighbors = minNeighbors;
        conf.minSizeFactor = minSizeFactor;
        conf.gpu = gpuMat != null || cudaMat != null;
        return conf;
    }

    @Override
    public OCVClassifier clone() {
        try {
            var cloneObj = (OCVClassifier) super.clone();
            cloneObj.grayMat = grayMat.clone();
            if (gpuMat != null) {
                cloneObj.gpuMat = gpuMat.clone();
            }
            if (cudaMat != null) {
                cloneObj.cudaMat = cudaMat.clone();
            }
            cloneObj.initClassifier();
            return cloneObj;
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
