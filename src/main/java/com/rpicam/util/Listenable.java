package com.rpicam.util;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public class Listenable<T> {
    private final Map<T, T> listeners = Collections.synchronizedMap(new WeakHashMap<>());

    public void addListener(T listener) {
        listeners.put(listener, listener);
    }

    public void addWeakListener(T listener) {
        listeners.put(listener, null);
    }

    public void removeListener(T listener) {
        listeners.remove(listener);
    }

    public Set<T> getListeners() {
        return listeners.keySet();
    }
}
