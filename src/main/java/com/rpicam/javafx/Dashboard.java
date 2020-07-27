package com.rpicam.javafx;

import com.rpicam.exceptions.UIException;
import com.rpicam.javafx.models.DashboardModel;
import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

public class Dashboard extends GridPane {
    @FXML
    private Label pageTitle;
    private ArchivesPage archivesPage;
    private CamerasPage camerasPage;
    private ScenesPage scenesPage;
    private SettingsPage settingsPage;
    private Node currentPage;

    private Timeline sidebarTimeline;

    private SimpleObjectProperty<DashboardModel> viewModel = new SimpleObjectProperty<>();

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
        archivesPage = new ArchivesPage();
        camerasPage = new CamerasPage();
        scenesPage = new ScenesPage();
        settingsPage = new SettingsPage();

        viewModel.addListener((obs, oldModel, newModel) -> {
            camerasPage.getViewModel().init(newModel.getAllCamerasScene());
        });

        viewModel.set(new DashboardModel());

        setupAnimations();
        showScenesPage();
    }

    private void setupAnimations() {
        var widthProperty = getColumnConstraints().get(0).maxWidthProperty();
        var maxWidth = getColumnConstraints().get(0).getMaxWidth();
        var minWidth = getColumnConstraints().get(0).getMinWidth();
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
            getChildren().remove(currentPage);
        }
        currentPage = page;
        add(currentPage, 1, 1);
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

    public DashboardModel getViewModel() {
        return viewModel.get();
    }

    public void setViewModel(DashboardModel aViewModel) {
        viewModel.set(aViewModel);
    }

    public ObjectProperty<DashboardModel> viewModelProperty() {
        return viewModel;
    }
}
