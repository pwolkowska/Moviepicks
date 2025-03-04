package pl.edu.pw.mini.movieapp.controllers;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import pl.edu.pw.mini.movieapp.models.Media;
import pl.edu.pw.mini.movieapp.models.Movie;
import pl.edu.pw.mini.movieapp.models.Series;
import pl.edu.pw.mini.movieapp.services.MovieService;
import pl.edu.pw.mini.movieapp.services.SeriesService;
import pl.edu.pw.mini.movieapp.services.TMDBClient;
import pl.edu.pw.mini.movieapp.utils.MediaCardUtil;

public class TrendingTodayController {

    @FXML
    private HBox trendingMoviesHBox; // HBox for trending movies

    @FXML
    private HBox trendingSeriesHBox; // HBox for trending series

    @FXML
    private ScrollPane trendingMoviesScrollPane; // ScrollPane for movies

    @FXML
    private ScrollPane trendingSeriesScrollPane; // ScrollPane for series

    private final MovieService movieService;
    private final SeriesService seriesService;
    private final TMDBClient client;

    public TrendingTodayController() {
        this.client = new TMDBClient();
        this.movieService = new MovieService(client);
        this.seriesService = new SeriesService(client);
    }

    @FXML
    public void initialize() {
        refreshTrendingContent(); // Load content on initialization
    }

    public void refreshTrendingContent() {
        // Trending Movies
        List<Movie> trendingMovies = movieService.getTrendingTodayMovies();
        if (trendingMovies != null && !trendingMovies.isEmpty()) {
            trendingMovies.forEach(movie -> trendingMoviesHBox.getChildren().add(createMediaCard(movie, false)));
        } else {
            System.out.println("no trending movies found!");
        }

        // Trending Series
        List<Series> trendingSeries = seriesService.getTrendingTodaySeries();
        if (trendingSeries != null && !trendingSeries.isEmpty()) {
            trendingSeries.forEach(series -> trendingSeriesHBox.getChildren().add(createMediaCard(series, true)));
        } else {
            System.out.println("no trending series found!");
        }
    }

    private VBox createMediaCard(Media media, boolean isSeries) {
        ImageView posterImageView = new ImageView();
        if (media.getPosterPath() != null) {
            posterImageView.setImage(new Image(media.getPosterPath()));
        }
        posterImageView.setFitWidth(180);
        posterImageView.setFitHeight(220);
        posterImageView.setPreserveRatio(true);

        Rectangle clip = new Rectangle(180, 220);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        posterImageView.setClip(clip);

        Label titleText = new Label(media.getTitle());
        titleText.setWrapText(true);
        titleText.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label dateText = null;
        if (!isSeries) {
            dateText = new Label("Release: " + media.getProductionDate());
            dateText.setStyle("-fx-font-size: 12px;");
        }else{
            dateText = new Label("Airing: " + media.getProductionDate());
            dateText.setStyle("-fx-font-size: 12px;");
        }

        VBox mediaCard = new VBox(10, posterImageView, titleText);
        if (dateText != null) {
            mediaCard.getChildren().add(dateText);
        }

        mediaCard.getStyleClass().add("media-card");
        mediaCard.setStyle("-fx-alignment: center; -fx-padding: 10; -fx-background-color: #e0e0e0; -fx-border-radius: 10; -fx-background-radius: 10;");
        mediaCard.setPrefSize(180, 300);

        titleText.setPrefHeight(40);
        if (dateText != null) {
            dateText.setPrefHeight(20);
        }

        // Prevent VBox from resizing children
        VBox.setVgrow(posterImageView, Priority.NEVER);
        VBox.setVgrow(titleText, Priority.ALWAYS);
        if (dateText != null) {
            VBox.setVgrow(dateText, Priority.NEVER);
        }

        // Add event handlers to the media card
        MediaCardUtil mediaCardUtil = new MediaCardUtil();
        mediaCardUtil.initializeMediaCardEvents(mediaCard, media, isSeries);

        return mediaCard;
    }
}