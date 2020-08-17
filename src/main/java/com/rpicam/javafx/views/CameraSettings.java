package com.rpicam.javafx.views;

import com.rpicam.exceptions.UIException;
import com.rpicam.javafx.util.View;
import com.rpicam.javafx.util.ViewModel;
import java.io.IOException;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

public class CameraSettings extends VBox implements View {
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
    private ComboBox<String> captureApiSelectBox;
    @FXML
    private TextField widthBox;
    @FXML
    private TextField heightBox;
    @FXML
    private TextField capRateBox;
    @FXML
    private TextField procIntervalBox;
    @FXML
    private ListView<String> classifierView;
    @FXML
    private CheckBox detectBoxToggle;
    @FXML
    private CheckBox statsToggle;

    private ToggleGroup sourceTg = new ToggleGroup();

    private SimpleMapProperty<String, String> results = new SimpleMapProperty<>();

    public CameraSettings() {
        final String FXML_PATH = "CameraSettings.fxml";
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(FXML_PATH));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        }
        catch (IOException ex) {
            throw new UIException("Failed to load " + FXML_PATH, ex);
        }
    }

    @FXML
    public void initialize() {
        urlRadioBtn.setToggleGroup(sourceTg);
        localRadioBtn.setToggleGroup(sourceTg);
        sourceTg.selectedToggleProperty().addListener((obs, oldSource, newSource) -> {
            var selectedBtn = (RadioButton) newSource;
            if (selectedBtn == urlRadioBtn) {
                urlTextBox.setDisable(false);
                cameraSelectBox.setDisable(true);
                captureApiSelectBox.setDisable(true);
            } else if (selectedBtn == localRadioBtn) {
                urlTextBox.setDisable(true);
                cameraSelectBox.setDisable(false);
                captureApiSelectBox.setDisable(false);
            }
        });
        sourceTg.selectToggle(urlRadioBtn);

        // TODO: Get API list from hardware detection module
        captureApiSelectBox.getItems().add("CAP_ANY");
        captureApiSelectBox.getItems().add("CAP_DSHOW");
        captureApiSelectBox.setValue("CAP_ANY");

        settingsAccordion.setExpandedPane(cameraSettingsPane);
    }

    public ReadOnlyMapProperty<String, String> resultsProperty() {
        return results;
    }

    @FXML
    private void onApplyClicked() {
        var resultsMap = FXCollections.<String, String>observableHashMap();
        var selectedSourceType = sourceTg.selectedToggleProperty().get();
        if (selectedSourceType == localRadioBtn) {
            resultsMap.put("type", "local");
            resultsMap.put("camIndex", cameraSelectBox.getValue());

        }
        else if (selectedSourceType == urlRadioBtn) {
            resultsMap.put("type", "path");
            resultsMap.put("url", urlTextBox.getText());
        }
        resultsMap.put("captureApi", captureApiSelectBox.getValue());
        resultsMap.put("widthRes", widthBox.getText());
        resultsMap.put("heightRes", heightBox.getText());
        resultsMap.put("capRate", capRateBox.getText());
        resultsMap.put("procInterval", procIntervalBox.getText());
        resultsMap.put("drawDetection", Boolean.toString(detectBoxToggle.isSelected()));
        resultsMap.put("drawStats", Boolean.toString(statsToggle.isSelected()));
        results.set(resultsMap);
    }

    @Override
    public ViewModel getViewModel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
