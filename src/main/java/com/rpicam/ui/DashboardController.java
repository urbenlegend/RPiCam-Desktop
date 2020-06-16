package com.rpicam.ui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;


public class DashboardController implements Initializable {
    @FXML
    private GridPane dashboardGrid;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }
    
    public GridPane getLayout() {
        return dashboardGrid;
    }
}
