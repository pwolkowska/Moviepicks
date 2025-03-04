package pl.edu.pw.mini.movieapp.controllers;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;

public class MainPageController {

    @FXML
    private VBox mainVBox; // This will hold the loaded trending today section

    public void initialize() {
        loadTrendingTodaySection();
    }

    private void loadTrendingTodaySection() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/trending-today.fxml"));
            VBox trendingTodaySection = loader.load();
            mainVBox.getChildren().add(trendingTodaySection); // Add to the main VBox
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
