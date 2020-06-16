package com.rpicam.video;

import java.io.ByteArrayInputStream;
import javafx.scene.image.Image;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;


public class VideoUtils {
    public static Image toJFXImage(Mat mat) {
        MatOfByte bytes = new MatOfByte();
        Imgcodecs.imencode("*.bmp", mat, bytes);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes.toArray());
        Image img = new Image(inputStream);
        return img;
    }
}
