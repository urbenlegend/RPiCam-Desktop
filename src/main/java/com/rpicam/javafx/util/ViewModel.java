package com.rpicam.javafx.util;

public interface ViewModel {
    default void onViewAdded() {};
    default void onViewRemoved() {};
}
