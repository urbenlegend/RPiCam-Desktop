package com.rpicam.exceptions;

public class VideoIOException extends RuntimeException {

    public VideoIOException() {
        super();
    }

    public VideoIOException(String s) {
        super(s);
    }

    public VideoIOException(String s, Throwable e) {
        super(s, e);
    }

    public VideoIOException(Throwable e) {
        super(e);
    }
}
