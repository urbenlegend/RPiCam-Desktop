package com.rpicam.video;


public class ClassifierResult {
    public final int x, y, w, h;
    public final String title;
    public final String color;

    public ClassifierResult(int aX, int aY, int aW, int aH, String aTitle, String aColor) {
        x = aX;
        y = aY;
        w = aW;
        h = aH;
        title = aTitle;
        color = aColor;
    }
}
