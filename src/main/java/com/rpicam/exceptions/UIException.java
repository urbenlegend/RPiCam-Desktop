/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.exceptions;

/**
 *
 * @author benrx
 */
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