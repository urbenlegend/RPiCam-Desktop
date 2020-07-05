package com.rpicam.ui;

import com.rpicam.exceptions.UIException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;


public class DashboardController implements Initializable {
    @FXML
    private GridPane dashboardGrid;
    @FXML
    private Label pageTitle;
    private Parent currentPage;
    private Parent scenesPage;
    private Parent camerasPage;
    private Parent archivesPage;
    private Parent settingsPage;
    private Timeline sidebarTimeline;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            scenesPage = FXMLLoader.load(getClass().getResource("ScenesPage.fxml"));
            camerasPage = FXMLLoader.load(getClass().getResource("CamerasPage.fxml"));
            archivesPage = FXMLLoader.load(getClass().getResource("ArchivesPage.fxml"));
            settingsPage = FXMLLoader.load(getClass().getResource("SettingsPage.fxml"));
        }
        catch (IOException ex) {
            throw new UIException("Dashboard failed to load sub pages", ex);
        }

        setupAnimations();
        showScenesPage();
    }

    private void setupAnimations() {
        var widthProperty = dashboardGrid.getColumnConstraints().get(0).maxWidthProperty();
        var maxWidth = dashboardGrid.getColumnConstraints().get(0).getMaxWidth();
        var minWidth = dashboardGrid.getColumnConstraints().get(0).getMinWidth();
        var kvSideBarOpen = new KeyValue(widthProperty, maxWidth);
        var kfSideBarOpen = new KeyFrame(Duration.millis(0), kvSideBarOpen);
        var kvSideBarClosed = new KeyValue(widthProperty, minWidth);
        var kfSideBarClosed = new KeyFrame(Duration.millis(160), kvSideBarClosed);
        sidebarTimeline = new Timeline();
        sidebarTimeline.getKeyFrames().addAll(kfSideBarOpen, kfSideBarClosed);
        sidebarTimeline.setRate(-1.0);
    }

    @FXML
    private void showScenesPage() {
        pageTitle.setText("Scenes");
        setPage(scenesPage);
    }

    @FXML
    private void showCamerasPage() {
        pageTitle.setText("Cameras");
        setPage(camerasPage);
    }

    @FXML
    private void showArchivesPage() {
        pageTitle.setText("Archives");
        setPage(archivesPage);
    }

    @FXML
    private void showSettingsPage() {
        pageTitle.setText("Settings");
        setPage(settingsPage);
    }

    private void setPage(Parent page) {
        if (currentPage != null) {
            dashboardGrid.getChildren().remove(currentPage);
        }
        currentPage = page;
        dashboardGrid.add(currentPage, 1, 1);
    }

    @FXML
    private void toggleSidebar() {
        sidebarTimeline.setRate(-sidebarTimeline.getRate());
        if (sidebarTimeline.getRate() < 0) {
            sidebarTimeline.playFrom("end");
        }
        else {
            sidebarTimeline.playFrom("start");
        }
    }
}
