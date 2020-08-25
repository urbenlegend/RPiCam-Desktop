package com.rpicam.cameras;

import com.rpicam.config.CameraConfig;
import com.rpicam.config.OCVLocalCameraConfig;
import com.rpicam.detection.ClassifierService;
import com.rpicam.exceptions.ConfigException;
import com.rpicam.exceptions.VideoIOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ServiceLoader;
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
    private int procInterval;

    private ScheduledExecutorService schedulePool;
    private ClassifierService classifierService = ServiceLoader.load(ClassifierService.class).findFirst().get();
    private final Mat capMat = new Mat();
    private final Mat bgraMat = new Mat();
    private final ByteBufferImage classifierFrame = new ByteBufferImage();
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
        conf.procInterval = procInterval;

        return conf;
    }

    @Override
    public void fromConfig(CameraConfig conf) {
        if (!(conf instanceof OCVLocalCameraConfig)) {
            throw new ConfigException("Invalid config for OCVLocalCamera");
        }

        var localConf = (OCVLocalCameraConfig) conf;
        camIndex = localConf.camIndex;
        captureApi = localConf.captureApi;
        widthRes = localConf.widthRes;
        heightRes = localConf.heightRes;
        capRate = localConf.capRate;
        procInterval = localConf.procInterval;
    }

    private void open() {
        setCameraName(String.format("%s: Camera %d", this.getClass().getSimpleName(), camIndex));
        setCameraStatus("Camera OK");

        int api;
        try {
            var apiField = opencv_videoio.class.getField(captureApi);
            api = apiField.getInt(null);
        } catch (Exception ex) {
            var newEx = new IllegalArgumentException("Invalid camera api specified: " + captureApi, ex);
            setCameraStatus(String.format("Camera ERROR: %s", newEx));
            throw newEx;
        }

        if (!capture.open(camIndex, api)) {
            var newEx = new VideoIOException("Could not open camera " + camIndex);
            setCameraStatus(String.format("Camera ERROR: %s", newEx));
            throw newEx;
        }

        if (widthRes > -1 && heightRes > -1) {
            capture.set(CAP_PROP_FRAME_WIDTH, widthRes);
            capture.set(CAP_PROP_FRAME_HEIGHT, heightRes);
        }
    }

    private void close() {
        capture.release();
    }

    @Override
    public void start() {
        if (schedulePool != null) {
            return;
        }
        open();
        schedulePool = Executors.newScheduledThreadPool(2);
        schedulePool.scheduleAtFixedRate(this::processFrame, 0, capRate, TimeUnit.MILLISECONDS);
        schedulePool.scheduleAtFixedRate(this::processClassifiers, 0, procInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        if (schedulePool != null) {
            schedulePool.shutdownNow();
            try {
                schedulePool.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
            }
            schedulePool = null;
        }
        close();
    }

    private void processFrame() {
        try {
            // Grab frame. Synchronize with classifier thread to make sure we
            // are not writing to buffer while classifiers are running
            synchronized (capMat) {
                if (!capture.read(capMat)) {
                    throw new VideoIOException("could not grab next frame from camera");
                }
            }

            // Convert to bgra for display
            cvtColor(capMat, bgraMat, COLOR_BGR2BGRA);
            var displayFrame = new ByteBufferImage(
                    bgraMat.createBuffer(),
                    bgraMat.cols(),
                    bgraMat.rows(),
                    ByteBufferImage.Format.BGRA);
            setFrame(displayFrame);

            // Calculate FPS
            var currentTime = LocalTime.now();
            var fpsCheckDuration = Duration.between(fpsLastCheck, currentTime);
            double fpsCheckSeconds = fpsCheckDuration.getSeconds() + (double) fpsCheckDuration.getNano() / 1000000000;
            if (fpsCheckSeconds >= 1) {
                double fps = fpsFrameCount / fpsCheckSeconds;
                setVideoStatus(String.format("%d x %d @ %.2f fps", capMat.cols(), capMat.rows(), fps));
                fpsLastCheck = currentTime;
                fpsFrameCount = 0;
            }

            setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            fpsFrameCount++;
        } catch (Throwable t) {
            // Log any exceptions and rethrow
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Camera did not finish processing the next frame!", t);
            setCameraStatus(String.format("Camera ERROR: %s", t));
            throw t;
        }
    }

    private void processClassifiers() {
        try {
            // Synchronize so that we don't copy buffer while it is being
            // written to from the frame thread.
            synchronized (capMat) {
                if (capMat.empty()) return;
                var image = new ByteBufferImage(capMat.createBuffer(), capMat.cols(), capMat.rows(), ByteBufferImage.Format.BGR);
                image.copyTo(classifierFrame);
            }

            var newClassifierResults = classifierService.submit(classifierFrame);
            setClassifierResults(newClassifierResults);
        } catch (InterruptedException e) {
        } catch (Throwable t) {
            // Log any exceptions and rethrow
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Camera did not finish classifying the next frame!", t);
            setCameraStatus(String.format("Camera ERROR: %s", t));
            throw t;
        }
    }
}
