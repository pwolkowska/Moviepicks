package pl.edu.pw.mini.movieapp.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import pl.edu.pw.mini.movieapp.models.Media;
import pl.edu.pw.mini.movieapp.models.Movie;
import pl.edu.pw.mini.movieapp.models.Series;
import pl.edu.pw.mini.movieapp.services.MovieService;
import pl.edu.pw.mini.movieapp.services.SeriesService;
import pl.edu.pw.mini.movieapp.services.TMDBClient;
import pl.edu.pw.mini.movieapp.utils.MediaCardUtil;
import pl.edu.pw.mini.movieapp.utils.SavedMedia;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class SavedMediaController {
    private final SavedMedia saved = SavedMedia.getInstance();
    private Map<Integer, Media> savedMedia = saved.getSavedMedia();
    @FXML
    private VBox savedMediaContainer;
    @FXML
    private Button saveButton;

    @FXML
    public void initialize() {
        loadSavedMedia();
        saveButton.setDisable(savedMedia.isEmpty());
        saveButton.setOnAction(event -> saveMediaToFile());

    }

    private void loadSavedMedia(){
        MediaCardUtil mediaCardUtil = new MediaCardUtil();
        savedMediaContainer.getChildren().clear();
        savedMedia.forEach((id, media) -> {
            VBox mediaCard = createMediaCard(media);
            boolean isSeries = media instanceof Series;
            mediaCardUtil.initializeMediaCardEvents(mediaCard, media, isSeries);
            savedMediaContainer.getChildren().add(mediaCard);
        });
    }

    private VBox createMediaCard(Media media){
        VBox mediaCard = new VBox();
        mediaCard.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 10; -fx-border-radius: 10; -fx-background-radius: 10; -fx-spacing: 10;");
        mediaCard.setPrefWidth(300);

        StackPane cardContent = new StackPane();
        // Layout for the image and title
        HBox content = new HBox();
        content.setSpacing(10);

        // ImageView for the media image
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

        Label dateLabel = new Label( (media.getProductionDate() != null ? media.getProductionDate() : "")+"\n");
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        Label genresLabel = new Label("\n\n\n"+(media.getGenres() != null ? String.join(", ", media.getGenres()) : ""));
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
        ratingLabel.setStyle("-fx-background-color: "+backgroundColor+ "; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 5; -fx-background-radius: 5; -fx-text-fill: white;");
        StackPane.setAlignment(ratingLabel, Pos.TOP_RIGHT);

        textContainer.getChildren().addAll(titleLabel, dateLabel, genresLabel);
        content.getChildren().addAll(imageView, textContainer);
        cardContent.getChildren().add(content);

        StackPane.setAlignment(ratingLabel, Pos.TOP_RIGHT);
        cardContent.getChildren().add(ratingLabel);

        mediaCard.getChildren().add(cardContent);

        return mediaCard;
    }

    private void saveMediaToFile(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Media List");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        Stage stage = (Stage) saveButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file!=null) {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(file));
                for (Map.Entry<Integer, Media> entry : savedMedia.entrySet()) {
                    Media media = entry.getValue();
                    String line = entry.getKey() + ";" + media.getTitle() + ";" + media.getProductionDate();
                    writer.write(line);
                    writer.newLine();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                try {
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}