package com.rpicam.ui;

import com.rpicam.exceptions.UIException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;


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
    
    // Mouse offsets used to implement decoration less window
    private double xOffset;
    private double yOffset;

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
        showScenesPage();
    }
    
    @FXML
    private void showScenesPage() {
        pageTitle.setText("Scenes");
        pagePane.getChildren().clear();
        pagePane.getChildren().add(scenesPage);
    }

    @FXML
    private void showCamerasPage() {
        pageTitle.setText("Cameras");
        pagePane.getChildren().clear();
        pagePane.getChildren().add(camerasPage);
    }

    @FXML
    private void showArchivesPage() {
        pageTitle.setText("Archives");
        pagePane.getChildren().clear();
        pagePane.getChildren().add(archivesPage);
    }

    @FXML
    private void showSettingsPage() {
        pageTitle.setText("Settings");
        pagePane.getChildren().clear();
        pagePane.getChildren().add(settingsPage);
    }
    
    @FXML
    private void titleBarPressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }
    
    @FXML
    private void titleBarDragged(MouseEvent event) {
        Stage stage = (Stage) dashboardGrid.getScene().getWindow();
        if (stage.isMaximized()) {
            var screens = Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());
            double screenWidth = screens.get(0).getBounds().getWidth();
            
            stage.setMaximized(false);
            
            double centeredX = event.getScreenX() - stage.getWidth() / 2;
            if (centeredX < 0) {
                stage.setX(0);
            }
            else if (centeredX + stage.getWidth() > screenWidth) {
                double newX = screenWidth - stage.getWidth();
                stage.setX(newX);
                xOffset = xOffset - newX;
            }
            else {
                stage.setX(centeredX);
                xOffset = stage.getWidth() / 2;
            }
        }
        else {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        }
    }
    
    @FXML
    private void minimizeWindow() {
        Stage stage = (Stage) dashboardGrid.getScene().getWindow();
        stage.setIconified(true);
    }
    
    @FXML
    private void maximizeWindow() {
        Stage stage = (Stage) dashboardGrid.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }
    
    @FXML
    private void closeWindow() {
        Stage stage = (Stage) dashboardGrid.getScene().getWindow();
        stage.close();
    }
}
