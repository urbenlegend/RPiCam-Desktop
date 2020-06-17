package com.rpicam.video;

import java.io.ByteArrayInputStream;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


public class VideoUtils {
    public static Image toJFXImage(Mat mat) {
        var bgraMat = new Mat();
        Imgproc.cvtColor(mat, bgraMat, Imgproc.COLOR_BGR2BGRA);
        
        var w = bgraMat.cols();
        var h = bgraMat.rows();
        var channels = 4;
        
        var imageArray = new byte[w * h * channels];
        bgraMat.get(0, 0, imageArray);
        var img = new WritableImage(w, h);
        var pw = img.getPixelWriter();
        pw.setPixels(0, 0, w, h, PixelFormat.getByteBgraPreInstance(), imageArray, 0, w * channels);
        return img;
    }
}
