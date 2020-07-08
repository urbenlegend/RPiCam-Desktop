package com.rpicam.ui;

import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
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
    private Accordion settingsAccordion;
    @FXML
    private TitledPane cameraSettingsPane;
    @FXML
    private RadioButton urlRadioBtn;
    @FXML
    private TextField urlTextBox;
    @FXML
    private RadioButton localRadioBtn;
    @FXML
    private ComboBox<String> cameraSelectBox;
    @FXML
    private ComboBox<String> apiSelectBox;
    @FXML
    private TextField widthBox;
    @FXML
    private TextField heightBox;
    @FXML
    private TextField capFpsBox;
    @FXML
    private TextField procFpsBox;
    @FXML
    private ListView<String> classifierView;
    @FXML
    private CheckBox detectBoxToggle;
    @FXML
    private CheckBox statsToggle;

    private ToggleGroup sourceTg = new ToggleGroup();

    private SimpleMapProperty<String, String> results = new SimpleMapProperty<>();

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

        // TODO: Get API list from hardware detection module
        apiSelectBox.getItems().add("CAP_ANY");
        apiSelectBox.getItems().add("CAP_DSHOW");
        apiSelectBox.setValue("CAP_ANY");

        settingsAccordion.setExpandedPane(cameraSettingsPane);
    }

    public ReadOnlyMapProperty<String, String> resultsProperty() {
        return results;
    }

    @FXML
    public void applyBtnClicked() {
        var resultsMap = FXCollections.<String, String>observableHashMap();
        var selectedSourceType = sourceTg.selectedToggleProperty().get();
        if (selectedSourceType == localRadioBtn) {
            resultsMap.put("type", "local");
            resultsMap.put("camIndex", cameraSelectBox.getValue().toString());
            resultsMap.put("api", apiSelectBox.getValue().toString());
        }
        else if (selectedSourceType == urlRadioBtn) {
            resultsMap.put("type", "url");
            resultsMap.put("url", urlTextBox.getText());
        }
        resultsMap.put("resW", widthBox.getText());
        resultsMap.put("resH", heightBox.getText());
        resultsMap.put("capFPS", capFpsBox.getText());
        resultsMap.put("procFPS", procFpsBox.getText());
        resultsMap.put("drawDetection", Boolean.toString(detectBoxToggle.isSelected()));
        resultsMap.put("drawStats", Boolean.toString(statsToggle.isSelected()));
        results.set(resultsMap);
    }
}
