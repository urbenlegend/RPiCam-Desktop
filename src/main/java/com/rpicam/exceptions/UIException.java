package com.rpicam.exceptions;

public class UIException extends RuntimeException {

    public UIException() {
        super();
    }

    public UIException(String s) {
        super(s);
    }

    public UIException(String s, Throwable e) {
        super(s, e);
    }

    public UIException(Throwable e) {
        super(e);
    }
}
