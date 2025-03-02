package pl.edu.pw.mini.movieapp.controllers;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import pl.edu.pw.mini.movieapp.app.App;

public class ToolbarController {

    @FXML
    private Button nameButton;

    @FXML
    private MenuButton moviesMenuButton;

    @FXML
    private MenuButton seriesMenuButton;

    @FXML
    private MenuButton savedMenuButton;

    @FXML
    private ScrollPane mainContent;

    @FXML
    public void initialize() {
        setupMovieMenuItems();
        setupSeriesMenuItems();
        setupSavedMenuItem();
        nameButton.setOnAction(event -> reloadMainPage());
    }

    private void setupMovieMenuItems() {
        for (MenuItem item : moviesMenuButton.getItems()) {
            item.setOnAction(event -> {
                System.out.println("Movie menu item clicked: " + item.getText());
                loadMediaPage(item.getText(), true);
            });
        }
    }

    private void setupSeriesMenuItems() {
        for (MenuItem item : seriesMenuButton.getItems()) {
            item.setOnAction(event -> {
                System.out.println("Loading series category: " + item.getText()); // Debug line
                loadMediaPage(item.getText(), false);
            });
        }
    }

    private void setupSavedMenuItem(){
        for (MenuItem item : savedMenuButton.getItems()) {
            item.setOnAction(event -> {
                System.out.println("Loading watchlist: " + item.getText()); // Debug line
                loadWatchlistPage();
            });
        }
    }

    private void reloadMainPage() {
        Stage stage = (Stage) nameButton.getScene().getWindow();
        stage.setScene(App.getMainScene());
    }

    private void loadMediaPage(String category, boolean isMovie) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/media-page.fxml"));
            ScrollPane mediaPage = loader.load();
            MediaPageController mediaPageController = loader.getController();
            
            // Get the filter controller from the included FXML
            FilterOptionsController filterController = (FilterOptionsController) mediaPageController.getFilterOptionsController();
            
            // Connect the controllers
            filterController.setMediaPageController(mediaPageController);
            
            // Set up the layout
            BorderPane layout = new BorderPane();
            layout.setCenter(mediaPage);
            
            // Update the scene
            Stage stage = (Stage) nameButton.getScene().getWindow();
            Scene scene = new Scene(layout, 1100, 700);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            stage.setScene(scene);
            
            // Show loading indicator immediately after scene is set
            Platform.runLater(() -> {
                mediaPageController.loadingIndicator.setVisible(true);
                mediaPageController.getMediaGrid().setVisible(false);
                // Load the media content
                mediaPageController.loadMedia(category, isMovie);
            });
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadWatchlistPage(){
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/watchlist-page.fxml"));
            Parent watchlistPage = loader.load();
            SavedMediaController controller = loader.getController();
            if (controller == null) return;
            Scene scene = new Scene(watchlistPage, 1100, 700);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

            Stage stage = (Stage) nameButton.getScene().getWindow();
            stage.setScene(scene);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}