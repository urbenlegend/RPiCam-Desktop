package com.rpicam.cameras;

import com.rpicam.detection.OCVClassifier;
import com.rpicam.config.OCVCameraConfig;
import com.rpicam.config.VlcjCameraConfig;
import com.rpicam.detection.ClassifierResult;
import com.rpicam.exceptions.ConfigException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bytedeco.javacpp.BytePointer;
import static org.bytedeco.opencv.global.opencv_core.CV_8UC4;
import org.bytedeco.opencv.opencv_core.Mat;
import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.base.MediaPlayer;
import uk.co.caprica.vlcj.player.embedded.EmbeddedMediaPlayer;
import uk.co.caprica.vlcj.player.embedded.videosurface.CallbackVideoSurface;
import static uk.co.caprica.vlcj.player.embedded.videosurface.VideoSurfaceAdapters.getVideoSurfaceAdapter;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormat;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.BufferFormatCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.RenderCallback;
import uk.co.caprica.vlcj.player.embedded.videosurface.callback.format.RV32BufferFormat;

public class VlcjCamera implements CameraWorker {
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private String url;
    private int procRate;
    private int procCount = 0;

    private final List<OCVClassifier> classifiers = Collections.synchronizedList(new ArrayList<>());

    private MediaPlayerFactory mediaPlayerFactory;
    private EmbeddedMediaPlayer player;
    private PixelBufferBufferFormatCallback bufferFormatCallback = new PixelBufferBufferFormatCallback();
    private PixelBufferRenderCallback renderCallback = new PixelBufferRenderCallback();
    private PixelBufferVideoSurface videoSurface = new PixelBufferVideoSurface();

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
        player.videoSurface().set(videoSurface);
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

    public void addClassifier(OCVClassifier c) {
        classifiers.add(c);
    }

    public void removeClassifier(OCVClassifier c) {
        classifiers.remove(c);
    }

    private void processFrame(ByteBuffer buffer, int width, int height) {
        pcs.firePropertyChange("frame", null, new ByteBufferImage(buffer, width, height));

        if (procCount % procRate == 0) {
            var capMat = new Mat(height, width, CV_8UC4, new BytePointer(buffer));

            var classifierResults = new ArrayList<ClassifierResult>();
            classifiers.forEach(c -> {
                classifierResults.addAll(c.apply(capMat));
            });

            pcs.firePropertyChange("classifierResults", null, classifierResults);
        }

        procCount++;
    }

    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    private class PixelBufferBufferFormatCallback implements BufferFormatCallback {
        @Override
        public BufferFormat getBufferFormat(int aSourceWidth, int aSourceHeight) {
            return new RV32BufferFormat(aSourceWidth, aSourceHeight);
        }

        @Override
        public void allocatedBuffers(ByteBuffer[] buffers) {
        }
    }

    private class PixelBufferRenderCallback implements RenderCallback {
        @Override
        public void display(MediaPlayer mediaPlayer, ByteBuffer[] nativeBuffers, BufferFormat bufferFormat) {
            VlcjCamera.this.processFrame(nativeBuffers[0], bufferFormat.getWidth(), bufferFormat.getHeight());
        }
    }

    private class PixelBufferVideoSurface extends CallbackVideoSurface {
        private PixelBufferVideoSurface() {
            super(
                VlcjCamera.this.bufferFormatCallback,
                VlcjCamera.this.renderCallback,
                true,
                getVideoSurfaceAdapter()
            );
        }
    }
}
