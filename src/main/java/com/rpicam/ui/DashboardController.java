package com.rpicam.ui;

import com.rpicam.exceptions.UIException;
import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

public class DashboardController {

    @FXML
    private GridPane dashboardGrid;
    @FXML
    private Label pageTitle;
    private FXMLLoader archivesLoader = new FXMLLoader(getClass().getResource("ArchivesPage.fxml"));
    private Parent archivesPage;
    private FXMLLoader camerasLoader = new FXMLLoader(getClass().getResource("CamerasPage.fxml"));
    private Parent camerasPage;
    private Parent currentPage;
    private FXMLLoader scenesLoader = new FXMLLoader(getClass().getResource("ScenesPage.fxml"));
    private Parent scenesPage;
    private FXMLLoader settingsLoader = new FXMLLoader(getClass().getResource("SettingsPage.fxml"));
    private Parent settingsPage;
    private Timeline sidebarTimeline;
    private MainApp app;

    @FXML
    public void initialize() {
        try {
            archivesPage = archivesLoader.load();
            camerasPage = camerasLoader.load();
            scenesPage = scenesLoader.load();
            settingsPage = settingsLoader.load();
        } catch (IOException ex) {
            throw new UIException("Dashboard failed to load sub pages", ex);
        }

        setupAnimations();
        showScenesPage();
    }

    public void setApp(MainApp aApp) {
        app = aApp;
        camerasLoader.<CamerasPageController>getController()
                .setModel(app.getCameraManager().getModel());
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
    private void toggleSidebar() {
        sidebarTimeline.setRate(-sidebarTimeline.getRate());
        if (sidebarTimeline.getRate() < 0) {
            sidebarTimeline.playFrom("end");
        } else {
            sidebarTimeline.playFrom("start");
        }
    }

    private void setPage(Parent page) {
        if (currentPage != null) {
            dashboardGrid.getChildren().remove(currentPage);
        }
        currentPage = page;
        dashboardGrid.add(currentPage, 1, 1);
    }

    @FXML
    private void showArchivesPage() {
        pageTitle.setText("Archives");
        setPage(archivesPage);
    }

    @FXML
    private void showCamerasPage() {
        pageTitle.setText("Cameras");
        setPage(camerasPage);
    }

    @FXML
    private void showScenesPage() {
        pageTitle.setText("Scenes");
        setPage(scenesPage);
    }

    @FXML
    private void showSettingsPage() {
        pageTitle.setText("Settings");
        setPage(settingsPage);
    }
}
