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

        try (Mat tempMat = bgraMat.getMat(ACCESS_READ)) {
            ByteBuffer buf = deepCopy(tempMat.createBuffer());
            var pixelBuf = new PixelBuffer<ByteBuffer>(tempMat.cols(), tempMat.rows(), buf, PixelFormat.getByteBgraPreInstance());
            return new WritableImage(pixelBuf);
        }
    }

    public static ByteBuffer deepCopy(ByteBuffer orig) {
        int pos = orig.position();
        int lim = orig.limit();
        try {
            orig.position(0).limit(orig.capacity()); // set range to entire buffer
            ByteBuffer toReturn = deepCopyVisible(orig); // deep copy range
            toReturn.position(pos).limit(lim); // set range to original
            return toReturn;
        }
        finally // do in finally in case something goes wrong we don't bork the orig
        {
            orig.position(pos).limit(lim); // restore original
        }
    }

    public static ByteBuffer deepCopyVisible(ByteBuffer orig) {
        int pos = orig.position();
        try {
            ByteBuffer toReturn;
            // try to maintain implementation to keep performance
            if (orig.isDirect()) {
                toReturn = ByteBuffer.allocateDirect(orig.remaining());
            } else {
                toReturn = ByteBuffer.allocate(orig.remaining());
            }

            toReturn.put(orig);
            toReturn.order(orig.order());

            return toReturn.position(0);
        }
        finally {
            orig.position(pos);
        }
    }
}
