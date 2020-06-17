package com.rpicam.ui;

import com.rpicam.exceptions.UIException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;


public class DashboardController implements Initializable {    
    @FXML
    private GridPane dashboardGrid;
    @FXML
    private HBox pagePane;
    @FXML
    private Label pageTitle;
    
    private Parent scenesPage;
    private Parent camerasPage;
    private Parent archivesPage;
    private Parent settingsPage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            scenesPage = FXMLLoader.load(getClass().getResource("Scenes.fxml"));
            camerasPage = FXMLLoader.load(getClass().getResource("Cameras.fxml"));
            archivesPage = FXMLLoader.load(getClass().getResource("Archives.fxml"));
            settingsPage = FXMLLoader.load(getClass().getResource("Settings.fxml"));
        }
        catch (IOException ex) {
            throw new UIException("Dashboard failed to load sub pages", ex);
        }
        showScenesPage(null);
    }
    
    public GridPane getLayout() {
        return dashboardGrid;
    }
    
    @FXML
    private void showScenesPage(MouseEvent event) {
        pageTitle.setText("Scenes");
        pagePane.getChildren().clear();
        pagePane.getChildren().add(scenesPage);
    }

    @FXML
    private void showCamerasPage(MouseEvent event) {
        pageTitle.setText("Cameras");
        pagePane.getChildren().clear();
        pagePane.getChildren().add(camerasPage);
    }

    @FXML
    private void showArchivesPage(MouseEvent event) {
        pageTitle.setText("Archives");
        pagePane.getChildren().clear();
        pagePane.getChildren().add(archivesPage);
    }

    @FXML
    private void showSettingsPage(MouseEvent event) {
        pageTitle.setText("Settings");
        pagePane.getChildren().clear();
        pagePane.getChildren().add(settingsPage);
    }
}
