package com.rpicam.javafx.views;

import com.rpicam.exceptions.UIException;
import com.rpicam.javafx.util.View;
import com.rpicam.javafx.util.ViewModel;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

public class ArchivesPage extends BorderPane implements View {
    public ArchivesPage() {
        final String FXML_PATH = "ArchivesPage.fxml";
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

    @Override
    public ViewModel getViewModel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
