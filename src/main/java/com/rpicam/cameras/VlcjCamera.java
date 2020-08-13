package com.rpicam.cameras;

import com.rpicam.config.OCVCameraConfig;
import com.rpicam.config.VlcjCameraConfig;
import com.rpicam.exceptions.ConfigException;
import java.nio.ByteBuffer;
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
    private int procCount = 0;

    private ByteBufferImage frame;
    private StatsResult statsResult;
    private ArrayList<ClassifierResult> classifierResults;

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
        player.events().addMediaPlayerEventListener(new VlcjCameraPlayerEventListener());
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
        var oldFrame = frame;
        frame = new ByteBufferImage(buffer, width, height);
        getPropertyChangeSupport().firePropertyChange("frame", oldFrame, frame);

        if (procCount % procRate == 0) {
            var oldClassifierResults = classifierResults;
            classifierResults = new ArrayList<>();
            getClassifiers().forEach(c -> {
                classifierResults.addAll(c.apply(frame));
            });
            getPropertyChangeSupport().firePropertyChange("classifierResults", oldClassifierResults, classifierResults);
        }

        // TODO: Implement real stats
        var oldStatsResult = statsResult;
        statsResult = new StatsResult(String.format("%s: %s", this.getClass().getSimpleName(), url),
                "Camera OK",
                "30",
                String.format("%d x %d", width, height),
                "",
                "");
        getPropertyChangeSupport().firePropertyChange("statsResult", oldStatsResult, statsResult);

        procCount++;
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

    private class VlcjCameraPlayerEventListener extends MediaPlayerEventAdapter {
        @Override
        public void error(MediaPlayer mediaPlayer) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Error!");
        }

        @Override
        public void stopped(MediaPlayer mediaPlayer) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Stopped!");
        }

        @Override
        public void playing(MediaPlayer mediaPlayer) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Playing!");
        }

        @Override
        public void opening(MediaPlayer mediaPlayer) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Opening!");
        }
    }
}
