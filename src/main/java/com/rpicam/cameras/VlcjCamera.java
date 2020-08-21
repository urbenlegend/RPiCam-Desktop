package com.rpicam.cameras;

import com.rpicam.detection.ClassifierResult;
import com.rpicam.config.OCVCameraConfig;
import com.rpicam.config.VlcjCameraConfig;
import com.rpicam.exceptions.ConfigException;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.base.MediaPlayerEventAdapter;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import static uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters.getVideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

public class VlcjCamera extends CameraWorker {
    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer player;
    private String url;
    private int procInterval;

    private ScheduledExecutorService schedulePool;
    private ExecutorService classifierPool;
    private final ByteBufferImage classifierFrame = new ByteBufferImage();
    private final ByteBufferImage frameCopy = new ByteBufferImage();
    private int fpsFrameCount = 0;
    private LocalTime fpsLastCheck = LocalTime.now();

    @Override
    public VlcjCameraConfig toConfig() {
        var conf = new VlcjCameraConfig();
        conf.url = url;
        conf.procInterval = procInterval;

        return conf;
    }

    @Override
    public void fromConfig(OCVCameraConfig conf) {
        if (!(conf instanceof VlcjCameraConfig)) {
            throw new ConfigException("Invalid config for OCVLocalCamera");
        }

        var localConf = (VlcjCameraConfig) conf;

        url = localConf.url;
        procInterval = localConf.procInterval;
    }

    private void open() {
        setCameraName(String.format("%s: %s", this.getClass().getSimpleName(), url));
        setCameraStatus("Camera OK");

        mediaPlayerFactory = new MediaPlayerFactory();
        player = mediaPlayerFactory.mediaPlayers().newEmbeddedMediaPlayer();
        player.videoSurface().set(new VlcjCameraSurface());
        player.events().addMediaPlayerEventListener(new VlcjCameraEventListener());
    }

    private void close() {
        player.release();
        mediaPlayerFactory.release();
    }

    @Override
    public void start() {
        if (schedulePool != null || classifierPool != null) {
            return;
        }
        open();
        schedulePool = Executors.newScheduledThreadPool(1);
        classifierPool = Executors.newSingleThreadExecutor();
        player.media().play(url);
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
        if (classifierPool != null) {
            classifierPool.shutdownNow();
            try {
                classifierPool.awaitTermination(1, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
            }
            classifierPool = null;
        }
        player.controls().stop();
        close();
    }

    private void processFrame(ByteBufferImage image) {
        try {
            synchronized (frameCopy) {
                image.copyTo(frameCopy);
            }
            setFrame(image);

            // Calculate FPS
            var currentTime = LocalTime.now();
            var fpsCheckDuration = Duration.between(fpsLastCheck, currentTime);
            double fpsCheckSeconds = fpsCheckDuration.getSeconds() + (double) fpsCheckDuration.getNano() / 1000000000;
            if (fpsCheckSeconds >= 1) {
                double fps = fpsFrameCount / fpsCheckSeconds;
                setVideoStatus(String.format("%d x %d @ %.2f fps", image.getWidth(), image.getHeight(), fps));
                fpsLastCheck = currentTime;
                fpsFrameCount = 0;
            }

            setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            fpsFrameCount++;
        } catch (Throwable t) {
            // Log any exceptions and rethrow
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Camera did not finishing processing the next frame!", t);
            setCameraStatus(String.format("Camera ERROR: %s", t));
            throw t;
        }
    }

    private void processClassifiers() {
        try {
            // Synchronize so that we don't copy buffer while it is being
            // written to from the frame thread.
            synchronized (frameCopy) {
                if (frameCopy.isEmpty()) return;
                frameCopy.copyTo(classifierFrame);
            }

            // Create classifier jobs
            var classifierJobs = getClassifiers().stream()
                    .map((c) -> {
                        Callable<List<ClassifierResult>> classifierJob = () -> {
                            return c.apply(classifierFrame);
                        };
                        return classifierJob;
                    })
                    .collect(Collectors.toList());

            // Feed jobs into classifier executor and wait for results
            var newClassifierResults = classifierPool.invokeAll(classifierJobs).stream()
                    .flatMap(resultFuture -> {
                        try {
                            return resultFuture.get().stream();
                        } catch (Exception e) {
                            return Stream.empty();
                        }
                    })
                    .collect(Collectors.toList());

            setClassifierResults(newClassifierResults);
        } catch (InterruptedException e) {
        } catch (Throwable t) {
            // Log any exceptions and rethrow
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Camera did not finish classifying the next frame!", t);
            setCameraStatus(String.format("Camera ERROR: %s", t));
            throw t;
        }
    }

    private class VlcjCameraSurface extends CallbackVideoSurface {
        private VlcjCameraSurface() {
            super(
                new BufferFormatCallback() {
                    @Override
                    public BufferFormat getBufferFormat(int aSourceWidth, int aSourceHeight) {
                        return new RV32BufferFormat(aSourceWidth, aSourceHeight);
                    }

                    @Override
                    public void allocatedBuffers(ByteBuffer[] buffers) {}
                },
                new RenderCallback() {
                    @Override
                    public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
                        var image = new ByteBufferImage(nativeBuffers[0], bufferFormat.getWidth(), bufferFormat.getHeight(), ByteBufferImage.Format.BGRA);
                        VlcjCamera.this.processFrame(image);
                    }
                },
                true,
                getVideoSurfaceAdapter()
            );
        }
    }

    private class VlcjCameraEventListener extends MediaPlayerEventAdapter {
        @Override
        public void error(MediaPlayer mediaPlayer) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "VlcjCamera reported an error");
            setCameraName(String.format("%s: %s", this.getClass().getSimpleName(), url));
            setCameraStatus(String.format("Camera ERROR: VlcjCameraEventListener reported an error"));
        }

        @Override
        public void stopped(MediaPlayer mediaPlayer) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "VlcjCamera stopped");
            setCameraName(String.format("%s: %s", this.getClass().getSimpleName(), url));
            setCameraStatus(String.format("Camera STOPPED: Source terminated"));
        }
    }
}
