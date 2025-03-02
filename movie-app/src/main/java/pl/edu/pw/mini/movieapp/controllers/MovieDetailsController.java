package pl.edu.pw.mini.movieapp.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import pl.edu.pw.mini.movieapp.models.Movie;
import pl.edu.pw.mini.movieapp.services.MovieService;
import pl.edu.pw.mini.movieapp.services.TMDBClient;
import pl.edu.pw.mini.movieapp.utils.MediaCardUtil;
import pl.edu.pw.mini.movieapp.utils.SavedMedia;

import java.util.List;

public class MovieDetailsController {
    @FXML
    private Label movieTitleLabel;
    @FXML
    private Label movieDescriptionLabel;
    @FXML
    private Label movieReleaseDateLabel;
    @FXML
    private Label movieGenreLabel;
    @FXML
    private Label movieRatingLabel;
    @FXML
    private Label movieLengthLabel;
    @FXML
    private Label movieCountryLabel;
    @FXML
    private Label movieLanguageLabel;
    @FXML
    private ImageView movieImageView;
    @FXML
    private Label movieYearLabel;
    @FXML
    private Label similarMoviesLabel;
    @FXML
    private Hyperlink movieTrailerLabel;
    @FXML
    private Button saveMovieButton;
    private MovieService movieService;
    @FXML
    private HBox similarMoviesContainer;
    private final SavedMedia saved = SavedMedia.getInstance();

    @FXML
    public void initialize() {
        TMDBClient tmdbClient = new TMDBClient();
        movieService = new MovieService(tmdbClient);
    }

    public void setMovieDetails(Movie movie) {
        movieService.getMovieDetails(movie);
        movieTitleLabel.setText(movie.getTitle());
        movieYearLabel.setText(" ("+movie.getYear()+")");
        movieDescriptionLabel.setText(movie.getOverview());
        movieReleaseDateLabel.setText(movie.getProductionDate());
        movieGenreLabel.setText( String.join(", ", movie.getGenres()));
        double roundedScore = Math.round(movie.getUserScore() * 10) / 10.0;
        movieRatingLabel.setText("Rating: " + roundedScore);
        movieLengthLabel.setText( movie.getLength() + " min");
        movieCountryLabel.setText("Country: " + movie.getCountry());
        movieLanguageLabel.setText("Language: " + movie.getLanguage());
        if (movie.getPosterPath() != null) {
            Image image = new Image(movie.getPosterPath());
            movieImageView.setImage(image);
        }
        loadSimilarMovies(movie);
        String trailerLink = movieService.getMovieTrailer(movie);
        if (trailerLink != null && !trailerLink.isEmpty()) {
            movieTrailerLabel.setText("Watch Trailer");
            movieTrailerLabel.setOnAction(event -> {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(trailerLink));
                } catch (Exception e) {
                    System.err.println("Error opening trailer link: " + e.getMessage());
                }
            });
        } else {
            movieTrailerLabel.setText("");
            movieTrailerLabel.setDisable(true);
        }
        updateSaveButtonState(movie);

        saveMovieButton.setOnAction(event -> {
            if (saved.isSaved(movie)) {
                saved.deleteMedia(movie);
            } else {
                saved.saveMedia(movie);
            }
            updateSaveButtonState(movie);
        });
    }

    private void loadSimilarMovies(Movie movie){
        List<Movie> movies = movieService.getSimilarMovies(movie);
        similarMoviesContainer.getChildren().clear();
        if (movies.isEmpty()) {
            similarMoviesLabel.setVisible(false);
        } else {
            similarMoviesLabel.setVisible(true);
        }
        for (Movie m : movies) {
            VBox mediaCard = setSimilarMovies(m);
            similarMoviesContainer.getChildren().add(mediaCard);
        }
    }

    public VBox setSimilarMovies(Movie movie){
        ImageView imageView = new ImageView();
        if (movie.getPosterPath() != null) {
            imageView.setImage(new Image(movie.getPosterPath()));
            imageView.setFitWidth(120);
            imageView.setFitHeight(180);
            imageView.setPreserveRatio(true);
        }
        Rectangle clip = new Rectangle(180, 220);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageView.setClip(clip);
        imageView.setStyle("-fx-alignment: center;");

        Label titleLabel = new Label(movie.getTitle());
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);
        double roundedScore = Math.round(movie.getUserScore() * 10) / 10.0;
        Label scoreText = new Label("User Score: " + roundedScore);
        scoreText.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label dateText = new Label("Release: " + movie.getProductionDate());
        dateText.setStyle("-fx-font-size: 12px;");

        VBox mediaCard = new VBox(10, imageView, titleLabel, scoreText, dateText);
        mediaCard.getStyleClass().add("media-card");
        mediaCard.setStyle("-fx-alignment: center; -fx-padding: 10; -fx-background-color: #e0e0e0; -fx-border-radius: 10; -fx-background-radius: 10;");
        mediaCard.setPrefSize(180, 300);


        titleLabel.setPrefHeight(40);
        if (dateText != null) {
            dateText.setPrefHeight(20);
        }

        VBox.setVgrow(imageView, Priority.NEVER);
        VBox.setVgrow(titleLabel, Priority.ALWAYS);
        if (dateText != null) {
            VBox.setVgrow(dateText, Priority.NEVER);
        }
        if (scoreText!=null){
            VBox.setVgrow(scoreText,Priority.NEVER);
        }

        MediaCardUtil mediaCardUtil = new MediaCardUtil();
        mediaCardUtil.initializeMediaCardEvents(mediaCard, movie, false);
        return mediaCard;
    }

    private void updateSaveButtonState(Movie movie) {
        if (saved.isSaved(movie)) {
            saveMovieButton.setText("Delete from your watchlist");
            saveMovieButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        } else {
            saveMovieButton.setText("Add to your watchlist");
            saveMovieButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        }
    }
}