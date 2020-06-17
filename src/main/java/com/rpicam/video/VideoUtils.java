package com.rpicam.video;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;


public class VideoUtils {
    public static Image toJFXImage(Mat mat) {
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
        
        var w = mat.cols();
        var h = mat.rows();
        var channels = 3;
        
        var imageArray = new byte[w * h * channels];
        mat.get(0, 0, imageArray);
        var img = new WritableImage(w, h);
        var pw = img.getPixelWriter();
        pw.setPixels(0, 0, w, h, PixelFormat.getByteRgbInstance(), imageArray, 0, w * channels);
        return img;
    }
}
