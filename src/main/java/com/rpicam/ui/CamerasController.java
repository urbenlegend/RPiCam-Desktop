/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.ui;

import com.rpicam.video.VideoManager;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;

/**
 * FXML Controller class
 *
 * @author benrx
 */
public class CamerasController implements Initializable {
    @FXML
    private BorderPane mainLayout;
    @FXML
    private ScrollPane cameraPane;
    @FXML
    private FlowPane cameraList;
    @FXML
    private Slider zoomSlider;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        var videoManager = VideoManager.getInstance();
        
        for (var worker : videoManager.getWorkers().values()) {
            var cameraView = new VideoView();
            cameraView.prefWidthProperty().bind(zoomSlider.valueProperty());
            cameraView.prefHeightProperty().bind(zoomSlider.valueProperty().multiply(3d/4d));
            worker.getModels().add(cameraView.getCameraModel());
            cameraList.getChildren().add(cameraView);
            
            // TODO: Remove test camera views
            var cameraView1 = new VideoView();
            cameraView1.prefWidthProperty().bind(zoomSlider.valueProperty());
            cameraView1.prefHeightProperty().bind(zoomSlider.valueProperty().multiply(3d/4d));
            worker.getModels().add(cameraView1.getCameraModel());
            cameraList.getChildren().add(cameraView1);
            
            var cameraView2 = new VideoView();
            cameraView2.prefWidthProperty().bind(zoomSlider.valueProperty());
            cameraView2.prefHeightProperty().bind(zoomSlider.valueProperty().multiply(3d/4d));
            worker.getModels().add(cameraView2.getCameraModel());
            cameraList.getChildren().add(cameraView2);
        }
    }    
}
