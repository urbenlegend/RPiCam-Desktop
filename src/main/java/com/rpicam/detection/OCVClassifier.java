package com.rpicam.detection;

import com.rpicam.config.OCVClassifierConfig;
import java.util.ArrayList;
import java.util.function.Function;
import static org.bytedeco.opencv.global.opencv_objdetect.CASCADE_SCALE_IMAGE;
import org.bytedeco.opencv.opencv_core.UMat;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

public class OCVClassifier implements Function<UMat, ArrayList<ClassifierResult>> {

    private transient CascadeClassifier classifier;
    private String color = "";
    private String path = "";
    private String title = "";

    public OCVClassifier(String aPath, String aTitle, String aColor) {
        setPath(aPath);
        color = aColor;
        title = aTitle;
    }

    @Override
    public ArrayList<ClassifierResult> apply(UMat frame) {
        var detectedObjs = new RectVector();
        int minSize = Math.round(frame.rows() * 0.1f);

        // TODO: Check if correct parameters are being used
        classifier.detectMultiScale(frame,
                detectedObjs,
                1.1,
                3,
                CASCADE_SCALE_IMAGE,
                new Size(minSize, minSize),
                new Size()
        );

        var results = new ArrayList<ClassifierResult>();
        for (var obj : detectedObjs.get()) {
            results.add(new ClassifierResult(obj.x(), obj.y(), obj.width(), obj.height(), title, color));
        }

        return results;
    }

    private void setPath(String aPath) {
        path = aPath;
        classifier = new CascadeClassifier();
        classifier.load(this.path);
    }

    public OCVClassifierConfig toConfig() {
        var conf = new OCVClassifierConfig();
        conf.path = path;
        conf.title = title;
        conf.color = color;
        return conf;
    }

    public void fromConfig(OCVClassifierConfig conf) {
        setPath(conf.path);
        title = conf.title;
        color = conf.color;
    }
}
