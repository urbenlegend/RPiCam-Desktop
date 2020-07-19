package com.rpicam.ui.models;

import com.rpicam.scenes.SceneInfo;
import com.rpicam.scenes.SceneListener;
import com.rpicam.scenes.ViewInfo;
import java.util.List;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class SceneModel implements SceneListener {
    private SceneInfo scene;
    private SimpleListProperty<ViewInfo> views = new SimpleListProperty<>(FXCollections.observableArrayList());

    public SceneModel(SceneInfo aScene) {
        scene = aScene;
        scene.addWeakListener(this);
        views.addAll(scene.getViews());
    }

    public void addViewInfo(ViewInfo view) {
        scene.addView(view);
    }

    @Override
    public void onViewsUpdated(List<ViewInfo> aViews) {
        views.setAll(aViews);
    }

    public SimpleListProperty<ViewInfo> viewsProperty() {
        return views;
    }
}
