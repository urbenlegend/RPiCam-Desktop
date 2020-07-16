package com.rpicam.ui;

import com.rpicam.exceptions.UIException;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

public class ArchivesPage extends BorderPane {
    private static final String FXML_PATH = "ArchivesPage.fxml";

    public ArchivesPage() {
        try {
            var loader = new FXMLLoader(getClass().getResource(FXML_PATH));
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        } catch (IOException ex) {
            throw new UIException("Failed to load " + FXML_PATH, ex);
        }
    }

    public void initialize() {
        // TODO
    }

}
