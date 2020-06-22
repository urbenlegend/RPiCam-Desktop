/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam;

import com.rpicam.ui.MainApp;
import org.bytedeco.opencv.global.opencv_core;

public class Main {
    public static void main(String[] args) {
        // Setup OpenCL
        opencv_core.setUseOpenCL(true);
        
        MainApp.main(args);
    }
}