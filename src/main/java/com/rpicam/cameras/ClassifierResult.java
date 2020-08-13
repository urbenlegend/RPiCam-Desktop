package com.rpicam.cameras;

public class ClassifierResult {
    public final int x, y, w, h;
    public final String title;
    public final String color;

    public ClassifierResult(int x, int y, int w, int h, String title, String color) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.title = title;
        this.color = color;
    }
}
