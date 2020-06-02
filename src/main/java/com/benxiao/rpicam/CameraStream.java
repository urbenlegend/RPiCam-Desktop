/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.benxiao.rpicam;

import com.benxiao.rpicam.ui.*;
import nu.pattern.OpenCV;

public class CameraStream {
    public static void main(String[] args) {
        OpenCV.loadLocally();
        CameraStreamApp.main(args);
    }
}