package com.rpicam.video;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rpicam.ui.models.CameraManagerModel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class CameraManager {

    private CameraManagerModel viewModel = new CameraManagerModel(this);
    private ArrayList<CameraWorker> cameras = new ArrayList<>();
    private ArrayList<OCVClassifier> classifiers = new ArrayList<>();

    public void loadConfigFile(Path configPath) throws IOException {
        var configStr = Files.readString(configPath, StandardCharsets.US_ASCII);
        JsonObject rootJsonObj = JsonParser.parseString(configStr).getAsJsonObject();
        var builder = new GsonBuilder();
        var gson = builder.create();

        JsonArray classifiersJsonArray = rootJsonObj.getAsJsonArray("classifiers");
        JsonArray camerasJsonArray = rootJsonObj.getAsJsonArray("cameras");

        classifiersJsonArray.forEach((element) -> {
            var template = gson.fromJson(element, OCVClassifier.class);
            var classifier = new OCVClassifier(template.getPath(), template.getTitle(), template.getColor());
            classifiers.add(classifier);
        });

        camerasJsonArray.forEach((element) -> {
            var cameraJson = element.getAsJsonObject();
            switch (cameraJson.get("type").getAsString()) {
                case "local" -> {
                    var newCamera = new OCVLocalCamera();
                    newCamera.fromJson(element.toString());
                    addCamera(newCamera);
                }
                case "path" -> {
                    var newCamera = new OCVStreamCamera();
                    newCamera.fromJson(element.toString());
                    addCamera(newCamera);
                }
                // TODO: Add other camera types
            }
        });
    }

    public void saveConfigFile(Path configPath) throws IOException {
        var builder = new GsonBuilder();
        var gson = builder.setPrettyPrinting().create();

        var classifiersJsonArray = new JsonArray();
        var camerasJsonArray = new JsonArray();
        for (var classifier : classifiers) {
            classifiersJsonArray.add(JsonParser.parseString(classifier.toJson()));
        }
        for (var camera : cameras) {
            camerasJsonArray.add(JsonParser.parseString(camera.toJson()));
        }

        var configObj = new JsonObject();
        configObj.add("classifiers", classifiersJsonArray);
        configObj.add("cameras", camerasJsonArray);
        String configJson = gson.toJson(configObj);

        Files.writeString(configPath, configJson);
    }

    public void startCameras() {
        for (var c : cameras) {
            c.start();
        }
    }

    public void stopCameras() {
        for (var c : cameras) {
            c.stop();
        }
    }

    public void addCamera(CameraWorker camera) {
        if (camera instanceof OCVLocalCamera) {
            var ocvCamera = (OCVLocalCamera) camera;
            for (var c : classifiers) {
                ocvCamera.addClassifier(c);
            }
        }

        cameras.add(camera);
        updateModel();
    }

    public void removeCamera(CameraWorker camera) {
        camera.stop();
        cameras.remove(camera);
        updateModel();
    }

    public CameraManagerModel getViewModel() {
        return viewModel;
    }

    private void updateModel() {
        viewModel.updateCameraList(cameras);
    }
}
