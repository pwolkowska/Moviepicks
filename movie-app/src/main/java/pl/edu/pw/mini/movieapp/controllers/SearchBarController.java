package pl.edu.pw.mini.movieapp.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import pl.edu.pw.mini.movieapp.models.Media;
import pl.edu.pw.mini.movieapp.models.Movie;
import pl.edu.pw.mini.movieapp.models.Series;
import pl.edu.pw.mini.movieapp.services.MovieService;
import pl.edu.pw.mini.movieapp.services.SeriesService;
import pl.edu.pw.mini.movieapp.services.TMDBClient;
import pl.edu.pw.mini.movieapp.utils.MediaCardUtil;

public class SearchBarController {
  @FXML
  Button movieOrSeriesButton;
  private boolean searchMovies = true;
  @FXML
  private TextField searchField;
  @FXML
  private VBox searchResultsContainer;
  private final MediaCardUtil mediaCardUtil = new MediaCardUtil();
  private SeriesService seriesService;
  private MovieService movieService;
  @FXML
  private ScrollPane searchResultsScrollPane;
  @FXML
  private ProgressIndicator searchProgress;
  @FXML
    public void initialize() {
      TMDBClient tmdbClient = new TMDBClient();
      movieService = new MovieService(tmdbClient);
      seriesService = new SeriesService(tmdbClient);
      searchResultsScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    @FXML
  private void setMovieOrSeriesButton(){
      if ("Movie".equals(movieOrSeriesButton.getText())) {
        movieOrSeriesButton.setText("Series");
        searchMovies = false;
      } else {
        movieOrSeriesButton.setText("Movie");
        searchMovies = true;
      }
    }

  @FXML
  private void findSearchQuery() {
    String query = searchField.getText();
    if (query == null || query.isEmpty()) {
      System.out.println("Search query cannot be empty");
      return;
    }
    
    // Show loading indicator immediately
    searchProgress.setVisible(true);
    searchResultsContainer.getChildren().clear();
    
    Thread searchThread = new Thread(() -> {
        List<Media> results = new ArrayList<>();
        
        if (searchMovies) {
            List<Movie> movieResults = movieService.getMovies(query, true);
            results.addAll(movieResults);
        } else {
            List<Series> seriesResults = seriesService.getSeries(query, true);
            results.addAll(seriesResults);
        }

        List<VBox> mediaCards = results.stream()
            .map(media -> createMediaCard(media))
            .collect(Collectors.toList());

      Platform.runLater(() -> {
        searchResultsContainer.getChildren().clear(); // Clear previous results

        if (results.isEmpty()) {
          Label noResultsLabel = new Label("No results found");
          noResultsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #888;");
          searchResultsContainer.getChildren().add(noResultsLabel); // Add "No results found" label
        } else {
          mediaCards.forEach(card -> searchResultsContainer.getChildren().add(card));
          if (!results.isEmpty()) {
            searchResultsScrollPane.setPrefHeight(320);
          }
        }

        searchProgress.setVisible(false); // Hide loading indicator
      });
    });
    
    searchThread.start();
  }

  public void showSearchResults(List<Media> searchResults) {
    // Clear previous results
    searchResultsContainer.getChildren().clear();

    // Loop through each result and create a media card
    for (Media media : searchResults) {
      VBox mediaCard = createMediaCard(media);
      boolean isSeries = media instanceof Series;

      mediaCardUtil.initializeMediaCardEvents(mediaCard, media, isSeries);

      searchResultsContainer.getChildren().add(mediaCard);
    }
  }

  private VBox createMediaCard(Media media) {
    VBox mediaCard = new VBox();
    mediaCard.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 10; -fx-border-radius: 10; -fx-background-radius: 10; -fx-spacing: 10;");
    mediaCard.prefWidthProperty().bind(searchResultsScrollPane.widthProperty().subtract(20)); // Marginesy

    StackPane cardContent = new StackPane();

    HBox content = new HBox();
    content.setSpacing(10);

    ImageView imageView = new ImageView();
    imageView.setFitWidth(100);
    imageView.setFitHeight(150);

    if (media.getPosterPath() != null) {
      imageView.setImage(new Image(media.getPosterPath()));
    }
    VBox textContainer = new VBox();
    textContainer.setSpacing(5);

    Label titleLabel = new Label(media.getTitle());
    titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

    Label dateLabel = new Label((media.getProductionDate() != null ? media.getProductionDate() : "") );
    dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

    Label genresLabel = new Label( (media.getGenres() != null ? String.join(", ", media.getGenres()) : ""));
    genresLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

    Label ratingLabel = new Label();
    double rating = media.getUserScore();
    double roundedRating = Math.round(rating * 10) / 10.0;
    ratingLabel.setText(String.valueOf(roundedRating));
    String backgroundColor;
    if (roundedRating < 2) {
      backgroundColor = "#8B0000";
    } else if (roundedRating < 4) {
      backgroundColor = "#FF0000";
    } else if (roundedRating < 6) {
      backgroundColor = "#FFA500";
    } else if (roundedRating < 8) {
      backgroundColor = "#008000";
    } else {
      backgroundColor = "#006400";
    }
    ratingLabel.setStyle("-fx-background-color: " + backgroundColor + "; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 5; -fx-background-radius: 5; -fx-text-fill: white;");
    StackPane.setAlignment(ratingLabel, Pos.TOP_RIGHT);

    textContainer.getChildren().addAll(titleLabel, dateLabel, genresLabel);
    content.getChildren().addAll(imageView, textContainer);
    cardContent.getChildren().add(content);

    StackPane.setAlignment(ratingLabel, Pos.TOP_RIGHT);
    cardContent.getChildren().add(ratingLabel);

    mediaCard.getChildren().add(cardContent);

    MediaCardUtil mediaCardUtil = new MediaCardUtil();
    mediaCardUtil.initializeMediaCardEvents(mediaCard, media, media instanceof Series);

    return mediaCard;
  }
}
