package com.rpicam.ui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;

public class CameraSettingsController implements Initializable {

    @FXML
    private RadioButton urlRadioBtn;
    @FXML
    private RadioButton localRadioBtn;
    @FXML
    private TextField urlTextBox;
    @FXML
    private ComboBox<?> apiSelectBox;
    @FXML
    private ComboBox<?> cameraSelectBox;
    @FXML
    private TextField widthBox;
    @FXML
    private TextField heightBox;
    @FXML
    private TextField fpsBox;
    @FXML
    private ListView<?> classifierView;
    @FXML
    private CheckBox statsToggle;
    @FXML
    private CheckBox detectBoxToggle;
    @FXML
    private Accordion settingsAccordion;
    @FXML
    private TitledPane cameraSettingsPane;

    private ToggleGroup sourceTg = new ToggleGroup();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        urlRadioBtn.setToggleGroup(sourceTg);
        localRadioBtn.setToggleGroup(sourceTg);
        sourceTg.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            var selectedBtn = (RadioButton) newVal;
            if (selectedBtn == urlRadioBtn) {
                urlTextBox.setDisable(false);
                apiSelectBox.setDisable(true);
                cameraSelectBox.setDisable(true);
            }
            else if (selectedBtn == localRadioBtn) {
                urlTextBox.setDisable(true);
                apiSelectBox.setDisable(false);
                cameraSelectBox.setDisable(false);
            }
        });
        sourceTg.selectToggle(urlRadioBtn);

        settingsAccordion.setExpandedPane(cameraSettingsPane);
    }

    @FXML
    public void applyBtnClicked() {

    }
}
