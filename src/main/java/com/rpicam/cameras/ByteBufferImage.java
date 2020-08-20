package com.rpicam.cameras;

import java.nio.ByteBuffer;

public class ByteBufferImage {
    public final ByteBuffer buffer;
    public final int width;
    public final int height;
    public Format format;

    public ByteBufferImage(ByteBuffer buffer, int width, int height, Format format) {
        this.buffer = buffer;
        this.width = width;
        this.height = height;
        this.format = format;
    }

    public static enum Format {
        RGB, RGBA, BGR, BGRA, GRAY
    }
}
