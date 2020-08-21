package com.rpicam.cameras;

import java.nio.ByteBuffer;

public class ByteBufferImage implements Cloneable {
    private ByteBuffer buffer;
    private int width;
    private int height;
    private Format format;

    public ByteBufferImage() {
        this.buffer = null;
        this.width = 0;
        this.height = 0;
        this.format = Format.NONE;
    }

    public ByteBufferImage(ByteBuffer buffer, int width, int height, Format format) {
        this.buffer = buffer;
        this.width = width;
        this.height = height;
        this.format = format;
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

    public Format getFormat() {
        return format;
    }

    public boolean isEmpty() {
        return buffer == null;
    }

    private ByteBuffer cloneByteBuffer(ByteBuffer original) {
        if (original == null) {
            return null;
        }

        // Create clone with same capacity as original.
        ByteBuffer clone = (original.isDirect())
                ? ByteBuffer.allocateDirect(original.capacity())
                : ByteBuffer.allocate(original.capacity());

        copyByteBuffer(original, clone);

        return clone;
    }

    private boolean copyByteBuffer(ByteBuffer src, ByteBuffer dest) {
        if (src != null && dest != null && src.capacity() <= dest.capacity()) {
            // Create a read-only copy of the src.
            // This allows reading from the src without modifying it.
            ByteBuffer readOnlyCopy = src.asReadOnlyBuffer();

            // Reset read-only copy's position and limit so we can copy the
            // entire buffer.
            readOnlyCopy.position(0).limit(readOnlyCopy.capacity());
            dest.put(readOnlyCopy);

            // Set clone to have the same order, position, and limit as original
            dest.order(src.order());
            dest.position(src.position()).limit(src.limit());

            return true;
        }
        else {
            return false;
        }
    }

    public void copyTo(ByteBufferImage dest) {
        if (!copyByteBuffer(buffer, dest.buffer)) {
            dest.buffer = cloneByteBuffer(buffer);
        }
        dest.width = width;
        dest.height = height;
        dest.format = format;
    }

    @Override
    public ByteBufferImage clone() {
        var cloneObj = new ByteBufferImage(cloneByteBuffer(buffer), width, height, format);
        return cloneObj;
    }

    public static enum Format {
        RGB, RGBA, BGR, BGRA, GRAY, NONE
    }
}
