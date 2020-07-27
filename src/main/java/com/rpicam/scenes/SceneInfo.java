package com.rpicam.scenes;

import com.rpicam.config.SceneConfig;
import com.rpicam.config.ViewConfig;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class SceneInfo {
    private ArrayList<ViewInfo> views = new ArrayList<>();

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
    }

    public void removeView(ViewInfo view) {
        views.remove(view);
    }

    public void clearViews() {
        views.clear();
    }

    public ArrayList<ViewInfo> getViews() {
        return views;
    }
}
