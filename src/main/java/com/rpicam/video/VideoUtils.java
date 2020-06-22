package com.rpicam.video;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import static org.bytedeco.opencv.global.opencv_core.ACCESS_READ;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2RGB;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.UMat;


public class VideoUtils {
    public static Image toJFXImage(UMat mat) {
        cvtColor(mat, mat, COLOR_BGR2RGB);
        
        var w = mat.cols();
        var h = mat.rows();
        var channels = 3;
        
        var imageArray = new byte[w * h * channels];
        try (Mat tempMat = mat.getMat(ACCESS_READ)) {
            tempMat.data().get(imageArray);
        }
        var img = new WritableImage(w, h);
        var pw = img.getPixelWriter();
        pw.setPixels(0, 0, w, h, PixelFormat.getByteRgbInstance(), imageArray, 0, w * channels);
        
        return img;
    }
}
