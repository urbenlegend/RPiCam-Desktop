package com.rpicam.cameras;

import java.nio.ByteBuffer;

public class ByteBufferImage {
    private ByteBuffer buffer;
    private int width;
    private int height;

    public ByteBufferImage(ByteBuffer aBuffer, int aWidth, int aHeight) {
        buffer = aBuffer;
        width = aWidth;
        height = aHeight;
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
