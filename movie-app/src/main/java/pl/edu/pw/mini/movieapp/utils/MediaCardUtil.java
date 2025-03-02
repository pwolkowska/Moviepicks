package pl.edu.pw.mini.movieapp.utils;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pl.edu.pw.mini.movieapp.controllers.MovieDetailsController;
import pl.edu.pw.mini.movieapp.controllers.SeriesDetailsController;
import pl.edu.pw.mini.movieapp.models.Media;
import pl.edu.pw.mini.movieapp.models.Movie;
import pl.edu.pw.mini.movieapp.models.Series;

public class MediaCardUtil {
  
  public void initializeMediaCardEvents(VBox mediaCard, Media media, boolean isSeries) {    
    // Add click handler to navigate to the details page
    mediaCard.setOnMouseClicked(event -> {
        showDetailsPage(mediaCard, media, isSeries);
    });

    // Add hover effects
    mediaCard.setOnMouseEntered(event -> {
        mediaCard.setStyle("-fx-cursor: hand; -fx-background-color: #d0d0d0; -fx-alignment: center; -fx-padding: 10; -fx-border-radius: 10; -fx-background-radius: 10;");
    });

    mediaCard.setOnMouseExited(event -> {
        mediaCard.setStyle("-fx-cursor: default; -fx-alignment: center; -fx-padding: 10; -fx-background-color: #e0e0e0; -fx-border-radius: 10; -fx-background-radius: 10;");
    });
  }

  private void showDetailsPage(VBox mediaCard, Media media, boolean isSeries) {
    try {
        String fxmlFile = isSeries ? "/series-details.fxml" : "/movie-details.fxml";
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        
        Parent root = loader.load();

        // Get the controller and set the media details
        if (isSeries) {
            SeriesDetailsController seriesDetailsController = loader.getController();
            seriesDetailsController.setSeriesDetails((Series) media);
        } else {
            MovieDetailsController movieDetailsController = loader.getController();
            movieDetailsController.setMovieDetails((Movie) media);
        }

        // Get the current stage and set the new scene
        Stage stage = (Stage) mediaCard.getScene().getWindow();
        Scene currentScene = stage.getScene();
        Scene detailsScene = new Scene(root, currentScene.getWidth(), currentScene.getHeight());
        detailsScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        stage.setScene(detailsScene);
    } catch (IOException e) {
        e.printStackTrace();
    }
  }   
}
