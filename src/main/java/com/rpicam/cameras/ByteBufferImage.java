package com.rpicam.cameras;

import java.nio.ByteBuffer;

public class ByteBufferImage {
    public final ByteBuffer buffer;
    public final int width;
    public final int height;

    public ByteBufferImage(ByteBuffer buffer, int width, int height) {
        this.buffer = buffer;
        this.width = width;
        this.height = height;
    }
}
