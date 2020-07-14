/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.util;

/**
 *
 * @author benrx
 */
public interface Listenable<T> {

    void addListener(T listener);

    void addWeakListener(T listener);

    void removeListener(T listener);
}
