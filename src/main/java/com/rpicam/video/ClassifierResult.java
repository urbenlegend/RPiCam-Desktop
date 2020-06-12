/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.video;

/**
 *
 * @author benrx
 */
public class ClassifierResult {
    public final String title;
    public final int r, g, b;
    public final int x, y, w, h;
    
    public ClassifierResult(int x, int y, int w, int h, String title, int r, int g, int b) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.title = title;
        this.r = r;
        this.g = g;
        this.b = b;
    }
}
