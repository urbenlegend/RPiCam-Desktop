/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam;

import com.rpicam.ui.MainApp;
import nu.pattern.OpenCV;

public class Main {
    public static void main(String[] args) {
        OpenCV.loadLocally();
        MainApp.main(args);
    }
}