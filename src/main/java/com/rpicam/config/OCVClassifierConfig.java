package com.rpicam.config;

public class OCVClassifierConfig extends ClassifierConfig {
    public String path = "";
    public double scaleFactor;
    public int minNeighbors;
    public float minSizeFactor;
    public boolean gpu;

    public OCVClassifierConfig() {
        type = "opencv";
    }
}
