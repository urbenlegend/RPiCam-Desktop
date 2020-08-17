package com.rpicam.detection;

import com.rpicam.cameras.ByteBufferImage;
import com.rpicam.config.OCVClassifierConfig;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bytedeco.javacpp.BytePointer;
import static org.bytedeco.opencv.global.opencv_core.CV_8UC4;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2GRAY;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import static org.bytedeco.opencv.global.opencv_objdetect.CASCADE_SCALE_IMAGE;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.UMat;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

public class OCVClassifier implements Function<ByteBufferImage, ArrayList<ClassifierResult>>, Cloneable {
    private Mat grayMat = new Mat();
    private UMat gpuMat;
    private CascadeClassifier classifier;
    private String color = "";
    private String path = "";
    private String title = "";

    public OCVClassifier(String path, String title, String color, boolean gpu) {
        this.path = path;
        this.color = color;
        this.title = title;

        initClassifier();

        // Allocate GPU memory if gpu flag is enabled
        if (gpu) {
            gpuMat = new UMat();
        }
    }

    private void initClassifier() {
        classifier = new CascadeClassifier();
        classifier.load(path);
    }

    @Override
    public ArrayList<ClassifierResult> apply(ByteBufferImage image) {
        var imageMat = new Mat(image.height, image.width, CV_8UC4, new BytePointer(image.buffer));
        // Convert frame to grayscale for better detection and efficiency
        cvtColor(imageMat, grayMat, COLOR_BGR2GRAY);

        var detectedObjs = new RectVector();
        if (gpuMat != null) {
            grayMat.copyTo(gpuMat);
            int minSize = Math.round(gpuMat.rows() * 0.1f);
            // TODO: Check if correct parameters are being used
            classifier.detectMultiScale(gpuMat,
                    detectedObjs,
                    1.1,
                    3,
                    CASCADE_SCALE_IMAGE,
                    new Size(minSize, minSize),
                    new Size()
            );
        }
        else {
            int minSize = Math.round(grayMat.rows() * 0.1f);
            // TODO: Check if correct parameters are being used
            classifier.detectMultiScale(grayMat,
                    detectedObjs,
                    1.1,
                    3,
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

    public OCVClassifierConfig toConfig() {
        var conf = new OCVClassifierConfig();
        conf.path = path;
        conf.title = title;
        conf.color = color;
        conf.gpu = gpuMat != null;
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
            cloneObj.initClassifier();
            return cloneObj;
        }
        catch (CloneNotSupportedException ex) {
            Logger.getLogger(OCVClassifier.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
