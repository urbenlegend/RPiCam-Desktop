/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.video;

import org.opencv.core.Mat;

/**
 *
 * @author benrx
 */
public interface OCVFrameHandler {
    public void handleFrame(Mat frame);
}
