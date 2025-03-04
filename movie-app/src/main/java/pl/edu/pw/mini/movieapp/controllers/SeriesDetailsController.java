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
import pl.edu.pw.mini.movieapp.models.Series;
import pl.edu.pw.mini.movieapp.services.SeriesService;
import pl.edu.pw.mini.movieapp.services.TMDBClient;
import pl.edu.pw.mini.movieapp.utils.MediaCardUtil;
import pl.edu.pw.mini.movieapp.utils.SavedMedia;

import java.util.List;

public class SeriesDetailsController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label releaseDateLabel;
    @FXML
    private Label overviewLabel;
    @FXML
    private Label seasonsLabel;
    @FXML
    private Label genreLabel;
    @FXML
    private Label ratingLabel;
    @FXML
    private Label countryLabel;
    @FXML
    private Label languageLabel;
    @FXML
    private ImageView imageView;
    @FXML
    private Label yearLabel;
    @FXML
    private Label similarSeriesLabel;
    @FXML
    private HBox similarSeriesContainer;
    @FXML
    private Hyperlink trailerLabel;

    @FXML
    private Button saveSeriesButton;
    private SeriesService seriesService;
    private final SavedMedia saved = SavedMedia.getInstance();


    @FXML
    public void initialize() {
        TMDBClient tmdbClient = new TMDBClient();
        seriesService = new SeriesService(tmdbClient);
    }

    public void setSeriesDetails(Series series) {
        seriesService.getSeriesDetails(series);
        titleLabel.setText(series.getTitle());
        yearLabel.setText(" ("+series.getYear()+")");
        releaseDateLabel.setText(series.getProductionDate());
        overviewLabel.setText(series.getOverview());
        seasonsLabel.setText(series.getSeasons()==1?"Season: "+ series.getSeasons():"Seasons: "+ series.getSeasons());
        languageLabel.setText("Language: "+series.getLanguage());
        countryLabel.setText("Country: "+series.getCountry());
        ratingLabel.setText("Popularity: "+series.getPopularity());
        genreLabel.setText(String.join(", ", series.getGenres()));
        if (series.getPosterPath() != null) {
            Image image = new Image(series.getPosterPath());
            imageView.setImage(image);
        }
        loadSimilarSeries(series);
        String trailerLink = seriesService.getSeriesTrailer(series);
        if (trailerLink != null && !trailerLink.isEmpty()) {
            trailerLabel.setText("Watch Trailer");
            trailerLabel.setOnAction(event -> {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(trailerLink));
                } catch (Exception e) {
                    System.err.println("Error opening trailer link: " + e.getMessage());
                }
            });
        } else {
            trailerLabel.setText("");
            trailerLabel.setDisable(true);
        }
        updateSaveButtonState(series);

        saveSeriesButton.setOnAction(event -> {
            if (saved.isSaved(series)) {
                saved.deleteMedia(series);
            } else {
                saved.saveMedia(series);
            }
            updateSaveButtonState(series);

        });
    }

    private void loadSimilarSeries(Series series){
        List<Series> ser = seriesService.getSimilarSeries(series);
        similarSeriesContainer.getChildren().clear();
        if (ser.isEmpty()) {
            similarSeriesLabel.setVisible(false);
        } else {
            similarSeriesLabel.setVisible(true);
        }
        for (Series s : ser) {
            VBox mediaCard = setSimilarSeries(s);
            similarSeriesContainer.getChildren().add(mediaCard);
        }
    }

    public VBox setSimilarSeries(Series series){
        ImageView imageView = new ImageView();
        if (series.getPosterPath() != null) {
            imageView.setImage(new Image(series.getPosterPath()));
            imageView.setFitWidth(120);
            imageView.setFitHeight(180);
            imageView.setPreserveRatio(true);
        }
        Rectangle clip = new Rectangle(180, 220);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageView.setClip(clip);
        imageView.setStyle("-fx-alignment: center;");

        Label titleLabel = new Label(series.getTitle());
        titleLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        titleLabel.setWrapText(true);
        double roundedScore = Math.round(series.getUserScore() * 10) / 10.0;
        Label scoreText = new Label("User Score: " + roundedScore); // Assuming getUserScore() returns a String or can be converted to String
        scoreText.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label dateText = new Label("Airing: " + series.getProductionDate());
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
        mediaCardUtil.initializeMediaCardEvents(mediaCard, series, true);
        return mediaCard;
    }

    private void updateSaveButtonState(Series series) {
        if (saved.isSaved(series)) {
            saveSeriesButton.setText("Delete from your watchlist");
            saveSeriesButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        } else {
            saveSeriesButton.setText("Add to your watchlist");
            saveSeriesButton.setStyle("-fx-background-color: green; -fx-text-fill: white;");
        }
    }
}
