/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.benxiao.rpicam.exceptions;

/**
 *
 * @author benrx
 */
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