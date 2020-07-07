package com.rpicam.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;

public class CameraSettingsController {

    @FXML
    private ComboBox<?> apiSelectBox;
    @FXML
    private ComboBox<?> cameraSelectBox;
    @FXML
    private TitledPane cameraSettingsPane;
    @FXML
    private ListView<?> classifierView;
    @FXML
    private CheckBox detectBoxToggle;
    @FXML
    private TextField capFpsBox;
    @FXML
    private TextField procFpsBox;
    @FXML
    private TextField heightBox;
    @FXML
    private RadioButton localRadioBtn;
    @FXML
    private Accordion settingsAccordion;
    @FXML
    private CheckBox statsToggle;
    @FXML
    private RadioButton urlRadioBtn;
    @FXML
    private TextField urlTextBox;
    @FXML
    private TextField widthBox;
    private ToggleGroup sourceTg = new ToggleGroup();
    private VideoListModel videoListModel;

    @FXML
    public void initialize() {
        urlRadioBtn.setToggleGroup(sourceTg);
        localRadioBtn.setToggleGroup(sourceTg);
        sourceTg.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            var selectedBtn = (RadioButton) newVal;
            if (selectedBtn == urlRadioBtn) {
                urlTextBox.setDisable(false);
                apiSelectBox.setDisable(true);
                cameraSelectBox.setDisable(true);
            } else if (selectedBtn == localRadioBtn) {
                urlTextBox.setDisable(true);
                apiSelectBox.setDisable(false);
                cameraSelectBox.setDisable(false);
            }
        });
        sourceTg.selectToggle(urlRadioBtn);

        settingsAccordion.setExpandedPane(cameraSettingsPane);
    }

    public void setModel(VideoListModel aVideoListModel) {
        videoListModel = aVideoListModel;
    }

    @FXML
    public void applyBtnClicked() {
        if (sourceTg.selectedToggleProperty().get() == localRadioBtn) {
            int camIndex = Integer.parseInt(cameraSelectBox.getValue().toString());
            String api = apiSelectBox.getValue().toString();
            int width = Integer.parseInt(widthBox.getText());
            int height = Integer.parseInt(heightBox.getText());
            int capFps = Integer.parseInt(capFpsBox.getText());
            int procFps = Integer.parseInt(procFpsBox.getText());
            videoListModel.addOCVLocalCamera(camIndex, api, width, height, 1000 / capFps, 1000 / procFps);
        }
    }
}
