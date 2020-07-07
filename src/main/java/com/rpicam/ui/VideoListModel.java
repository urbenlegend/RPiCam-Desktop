package com.rpicam.ui;

import com.rpicam.video.OCVLocalCamera;
import com.rpicam.video.VideoManager;
import java.util.List;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class VideoListModel {

    private VideoManager videoManager;
    private SimpleListProperty<VideoModel> selection = new SimpleListProperty<>(FXCollections.observableArrayList());
    private SimpleListProperty<VideoModel> videoList = new SimpleListProperty<>(FXCollections.observableArrayList());

    public VideoListModel(VideoManager aVideoManager) {
        videoManager = aVideoManager;
    }

    public void updateVideoList(List<VideoModel> aVideoList) {
        videoList.setAll(aVideoList);
    }

    public void addOCVLocalCamera(int camIndex, String api, int resW, int resH, int capRate, int procRate) {
        var worker = new OCVLocalCamera();
        var options = worker.getOptions();
        options.camIndex = camIndex;
        options.api = api;
        options.resW = resW;
        options.resH = resH;
        options.capRate = capRate;
        options.procRate = procRate;
        videoManager.addWorker(worker);
        worker.start();
    }

    public void removeSelected() {
        for (var model : selection) {
            videoManager.removeWorkerViaModel(model);
        }
    }

    public SimpleListProperty<VideoModel> selectionProperty() {
        return selection;
    }

    public SimpleListProperty<VideoModel> videoListProperty() {
        return videoList;
    }
}
