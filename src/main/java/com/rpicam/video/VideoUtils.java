package com.rpicam.video;

import java.io.ByteArrayInputStream;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;


public class VideoUtils {
    public static Image toJFXImage(Mat mat) {
        // TODO: Figure out a way to convert without encoding
        MatOfByte bytes = new MatOfByte();
        Imgcodecs.imencode("*.bmp", mat, bytes);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes.toArray());
        Image img = new Image(inputStream);
        return img;
    }
    
    public static Image toJFXImageFast(Mat mat) {
        var w = mat.cols();
        var h = mat.rows();
        var imageArray = new byte[w * h * 3];
        mat.get(0, 0, imageArray);
        var img = new WritableImage(w, h);
        var pw = img.getPixelWriter();
        pw.setPixels(0, 0, w, h, PixelFormat.getByteRgbInstance(), imageArray, 0, w * 3);
        return img;
    }
}
