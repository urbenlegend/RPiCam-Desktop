package com.rpicam.exceptions;

public class ConfigException extends RuntimeException {

    public ConfigException() {
        super();
    }

    public ConfigException(String s) {
        super(s);
    }

    public ConfigException(String s, Throwable e) {
        super(s, e);
    }

    public ConfigException(Throwable e) {
        super(e);
    }
}
