package com.rpicam.cameras;

import com.rpicam.config.OCVCameraConfig;
import com.rpicam.config.VlcjCameraConfig;
import com.rpicam.exceptions.ConfigException;
import java.beans.PropertyChangeSupport;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private int procRate;

    private int totalFrames = 0;
    private int fpsFrameCount = 0;
    private LocalTime fpsLastCheck = LocalTime.now();

    @Override
    public VlcjCameraConfig toConfig() {
        var conf = new VlcjCameraConfig();
        conf.url = url;
        conf.procRate = procRate;

        return conf;
    }

    @Override
    public void fromConfig(OCVCameraConfig conf) {
        if (!(conf instanceof VlcjCameraConfig)) {
            throw new ConfigException("Invalid config for OCVLocalCamera");
        }

        var localConf = (VlcjCameraConfig) conf;

        url = localConf.url;
        procRate = localConf.procRate;
    }

    private void open() {
        // TODO: Throw exception if open fails!
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
        open();
        player.media().play(url);
    }

    @Override
    public void stop() {
        player.controls().stop();
        close();
    }

    private void processFrame(ByteBuffer buffer, int width, int height) {
        PropertyChangeSupport pcs = getPropertyChangeSupport();

        try {
            pcs.firePropertyChange("cameraName", null, String.format("%s: %s", this.getClass().getSimpleName(), url));
            var frame = new ByteBufferImage(buffer, width, height);
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
                pcs.firePropertyChange("videoQuality", null, String.format("%d x %d @ %.2f fps", width, height, fps));
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
                        VlcjCamera.this.processFrame(nativeBuffers[0], bufferFormat.getWidth(), bufferFormat.getHeight());
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
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Error!");
            getPropertyChangeSupport().firePropertyChange("cameraStatus", null, String.format("Camera ERROR: VlcjCameraEventListener reported an error"));
        }
    }
}
