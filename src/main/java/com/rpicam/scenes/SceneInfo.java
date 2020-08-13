package com.rpicam.scenes;

import com.rpicam.config.SceneConfig;
import com.rpicam.config.ViewConfig;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class SceneInfo {
    private ArrayList<ViewInfo> views = new ArrayList<>();

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void fromConfig(SceneConfig config) {
        for (var viewConfig : config.views) {
            var view = new ViewInfo();
            view.fromConfig(viewConfig);
            views.add(view);
        }
    }

    public SceneConfig toConfig() {
        var config = new SceneConfig();
        config.views = new ViewConfig[views.size()];
        views.stream()
                .map(view -> view.toConfig())
                .collect(Collectors.toList())
                .toArray(config.views);
        return config;
    }

    public void addView(ViewInfo view) {
        views.add(view);
        pcs.firePropertyChange("views", null, views);
    }

    public void removeView(ViewInfo view) {
        views.remove(view);
        pcs.firePropertyChange("views", null, views);
    }

    public void clearViews() {
        views.clear();
        pcs.firePropertyChange("views", null, views);
    }

    public ArrayList<ViewInfo> getViews() {
        return views;
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }

    public void removeAllPropertyChangeListeners() {
        for (var l : pcs.getPropertyChangeListeners()) {
            pcs.removePropertyChangeListener(l);
        }
    }
}
