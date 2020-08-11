package com.rpicam.cameras;

import java.nio.ByteBuffer;

public class ByteBufferImage {
    private ByteBuffer buffer;
    private int width;
    private int height;

    public ByteBufferImage(ByteBuffer buffer, int width, int height) {
        this.buffer = buffer;
        this.width = width;
        this.height = height;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
