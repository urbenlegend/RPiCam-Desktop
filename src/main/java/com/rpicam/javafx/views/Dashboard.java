package com.rpicam.javafx.views;

import com.rpicam.javafx.viewmodels.DashboardModel;
import com.rpicam.exceptions.UIException;
import com.rpicam.javafx.util.View;
import com.rpicam.javafx.util.ViewModel;
import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

public class Dashboard extends GridPane implements View {
    @FXML
    private Label pageTitle;
    private CamerasPage camerasPage;
    private ScenesPage scenesPage;
    private ArchivesPage archivesPage;
    private SettingsPage settingsPage;
    private Node currentPage;

    private Timeline sidebarTimeline;

    private DashboardModel viewModel = new DashboardModel();

    public Dashboard() {
        final String FXML_PATH = "Dashboard.fxml";
        try {
            var loader = new FXMLLoader(getClass().getResource(FXML_PATH));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        } catch (IOException ex) {
            throw new UIException("Failed to load " + FXML_PATH, ex);
        }
    }

    @FXML
    public void initialize() {
        camerasPage = new CamerasPage();
        scenesPage = new ScenesPage();
        archivesPage = new ArchivesPage();
        settingsPage = new SettingsPage();

        setupAnimations();
        showCamerasPage();
    }

    private void setupAnimations() {
        var widthProperty = getColumnConstraints().get(0).maxWidthProperty();
        double maxWidth = getColumnConstraints().get(0).getMaxWidth();
        double minWidth = getColumnConstraints().get(0).getMinWidth();
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
        if (currentPage == page) {
            return;
        }
        if (currentPage != null) {
            getChildren().remove(currentPage);
        }
        currentPage = page;
        add(currentPage, 1, 1);
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
    private void showArchivesPage() {
        pageTitle.setText("Archives");
        setPage(archivesPage);
    }

    @FXML
    private void showSettingsPage() {
        pageTitle.setText("Settings");
        setPage(settingsPage);
    }

    @Override
    public ViewModel getViewModel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
