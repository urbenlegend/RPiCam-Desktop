package com.rpicam.util;

import java.nio.ByteBuffer;
import javafx.scene.image.Image;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import static org.bytedeco.opencv.global.opencv_core.ACCESS_READ;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.UMat;

public class VideoUtils {
    public static Image wrapBgraUMat(UMat bgraMat) {
        try (Mat tempMat = bgraMat.getMat(ACCESS_READ)) {
            var pixelBuf = new PixelBuffer<ByteBuffer>(tempMat.cols(), tempMat.rows(), tempMat.createBuffer(), PixelFormat.getByteBgraPreInstance());
            return new WritableImage(pixelBuf);
        }
    }
}
