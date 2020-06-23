package com.rpicam.video;

import java.nio.ByteBuffer;
import javafx.scene.image.Image;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import static org.bytedeco.opencv.global.opencv_core.ACCESS_READ;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2BGRA;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.UMat;


public class VideoUtils {
    public static Image toJFXImage(UMat mat) {
        UMat bgraMat = new UMat();
        cvtColor(mat, bgraMat, COLOR_BGR2BGRA);
        
        var w = bgraMat.cols();
        var h = bgraMat.rows();
        
        try (Mat tempMat = bgraMat.getMat(ACCESS_READ)) {
            var pixelBuf = new PixelBuffer<ByteBuffer>(w, h, tempMat.createBuffer(), PixelFormat.getByteBgraPreInstance());
            return new WritableImage(pixelBuf);
        }    
    }
}
