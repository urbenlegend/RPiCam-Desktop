/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.ui;

import com.rpicam.video.VideoManager;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;

/**
 * FXML Controller class
 *
 * @author benrx
 */
public class CamerasController implements Initializable {
    @FXML
    private BorderPane cameraPane;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO: Replace with code that displays multiple videos
        var videoManager = VideoManager.getInstance();
        var worker = videoManager.getWorker(UUID.fromString("dd243140-b03a-4d72-b5ce-8f31412af8a5"));
        var cameraView = new VideoView();
        worker.bindTo(cameraView.getCameraModel());
        cameraPane.setCenter(cameraView);
    }    
    
}