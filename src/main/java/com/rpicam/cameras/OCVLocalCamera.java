package com.rpicam.cameras;

import com.rpicam.detection.ClassifierResult;
import com.rpicam.config.OCVCameraConfig;
import com.rpicam.config.OCVLocalCameraConfig;
import com.rpicam.exceptions.ConfigException;
import com.rpicam.exceptions.VideoIOException;
import java.beans.PropertyChangeSupport;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.bytedeco.opencv.global.opencv_imgproc.COLOR_BGR2BGRA;
import static org.bytedeco.opencv.global.opencv_imgproc.cvtColor;
import org.bytedeco.opencv.global.opencv_videoio;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_HEIGHT;
import static org.bytedeco.opencv.global.opencv_videoio.CAP_PROP_FRAME_WIDTH;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

public class OCVLocalCamera extends CameraWorker {
    private VideoCapture capture = new VideoCapture();
    private int camIndex;
    private String captureApi;
    private int widthRes;
    private int heightRes;
    private int capRate;
    private int procRate;

    private ScheduledExecutorService schedulePool;
    private final Mat capMat = new Mat();
    private final Mat bgraMat = new Mat();
    private int totalFrames = 0;
    private int fpsFrameCount = 0;
    private LocalTime fpsLastCheck = LocalTime.now();

    @Override
    public OCVLocalCameraConfig toConfig() {
        var conf = new OCVLocalCameraConfig();
        conf.camIndex = camIndex;
        conf.captureApi = captureApi;
        conf.widthRes = widthRes;
        conf.heightRes = heightRes;
        conf.capRate = capRate;
        conf.procRate = procRate;

        return conf;
    }

    @Override
    public void fromConfig(OCVCameraConfig conf) {
        if (!(conf instanceof OCVLocalCameraConfig)) {
            throw new ConfigException("Invalid config for OCVLocalCamera");
        }

        var localConf = (OCVLocalCameraConfig) conf;
        camIndex = localConf.camIndex;
        captureApi = localConf.captureApi;
        widthRes = localConf.widthRes;
        heightRes = localConf.heightRes;
        capRate = localConf.capRate;
        procRate = localConf.procRate;
    }

    private void open() {
        int api;
        try {
            var apiField = opencv_videoio.class.getField(captureApi);
            api = apiField.getInt(null);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid camera api specified: " + captureApi, ex);
        }

        if (!capture.open(camIndex, api)) {
            throw new VideoIOException("Could not open camera " + camIndex);
        }

        if (widthRes > 0 && heightRes > 0) {
            capture.set(CAP_PROP_FRAME_WIDTH, widthRes);
            capture.set(CAP_PROP_FRAME_HEIGHT, heightRes);
        }
    }

    private void close() {
        capture.release();
    }

    @Override
    public void start() {
        if (schedulePool != null || capture.isOpened()) {
            return;
        }
        open();
        schedulePool = Executors.newScheduledThreadPool(1);
        schedulePool.scheduleAtFixedRate(this::processFrame, 0, capRate, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        if (schedulePool != null) {
            schedulePool.shutdownNow();
            schedulePool = null;
        }
        close();
    }

    private void processFrame() {
        PropertyChangeSupport pcs = getPropertyChangeSupport();

        try {
            pcs.firePropertyChange("cameraName", null, String.format("%s: Camera %d", this.getClass().getSimpleName(), camIndex));
            if (!capture.read(capMat)) {
                throw new VideoIOException("could not grab next frame from camera");
            }
            cvtColor(capMat, bgraMat, COLOR_BGR2BGRA);
            var frame = new ByteBufferImage(bgraMat.createBuffer(), bgraMat.cols(), bgraMat.rows());
            pcs.firePropertyChange("frame", null, frame);

            if (totalFrames % procRate == 0) {
                var classifierResults = new ArrayList<ClassifierResult>();
                getClassifiers().forEach(c -> {
                    classifierResults.addAll(c.apply(frame));
                });
                pcs.firePropertyChange("classifierResults", null, classifierResults);
            }

            // Calculate FPS
            var currentTime = LocalTime.now();
            var fpsCheckDuration = Duration.between(fpsLastCheck, currentTime);
            double fpsCheckSeconds = fpsCheckDuration.getSeconds() + (double) fpsCheckDuration.getNano() / 1000000000;
            if (fpsCheckSeconds >= 1) {
                double fps = fpsFrameCount / fpsCheckSeconds;
                pcs.firePropertyChange("videoQuality", null, String.format("%d x %d @ %.2f fps", capMat.cols(), capMat.rows(), fps));
                fpsLastCheck = currentTime;
                fpsFrameCount = 0;
            }

            // Fire off stat changes
            pcs.firePropertyChange("cameraStatus", null, "Camera OK");
            pcs.firePropertyChange("timestamp", null, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            fpsFrameCount++;
            totalFrames++;
        }
        catch (Throwable t) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Camera did not finishing processing the next frame!", t);
            pcs.firePropertyChange("cameraStatus", null, String.format("Camera ERROR: %s", t));
        }
    }
}
