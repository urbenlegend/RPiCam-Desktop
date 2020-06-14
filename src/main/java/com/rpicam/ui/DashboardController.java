/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.rpicam.ui;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;

/**
 * FXML Controller class
 *
 * @author benrx
 */
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
