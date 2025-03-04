package pl.edu.pw.mini.movieapp.controllers;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import pl.edu.pw.mini.movieapp.models.Media;
import pl.edu.pw.mini.movieapp.services.MovieService;
import pl.edu.pw.mini.movieapp.services.SeriesService;
import pl.edu.pw.mini.movieapp.services.TMDBClient;
import pl.edu.pw.mini.movieapp.utils.MediaCardUtil;

public class MediaPageController {

    @FXML
    private Label categoryLabel;

    @FXML
    public GridPane mediaGrid;

    @FXML
    private FilterOptionsController filterOptionsController;

    @FXML
    public ProgressIndicator loadingIndicator;

    @FXML
    private Button loadMoreButton;

    private MovieService movieService;
    private SeriesService seriesService;
    private boolean isMovie;
    public List<? extends Media> allMedia = new ArrayList<>();
    public int currentDisplayed = 0;
    private static final int ITEMS_PER_PAGE = 20;

    @FXML
    public void initialize() {
        TMDBClient tmdbClient = new TMDBClient();
        movieService = new MovieService(tmdbClient);
        seriesService = new SeriesService(tmdbClient);
        
        mediaGrid.setHgap(20);
        mediaGrid.setVgap(20);
        mediaGrid.setPadding(new Insets(20));
        
        loadMoreButton.setOnAction(event -> loadMoreItems());
    }

    public void loadMedia(String category, boolean isMovie) {
        if (filterOptionsController == null) {
            System.out.println("Warning: FilterOptionsController is null!");
            return;
        }
        
        this.isMovie = isMovie;
        String mediaType = isMovie ? "Movies" : "Series";
        categoryLabel.setText(category + " " + mediaType);
        
        filterOptionsController.setCurrentCategory(category, isMovie);
        filterOptionsController.applyFilters();
    }

    public GridPane getMediaGrid() {
        return mediaGrid;
    }

    public VBox createMediaCard(Media media, boolean isSeries) {
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

        posterImageView.setStyle("-fx-alignment: center;");

        Label titleText = new Label(media.getTitle());
        titleText.setWrapText(true);
        titleText.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        double roundedScore = Math.round(media.getUserScore() * 10) / 10.0;
        Label scoreText = new Label("User Score: " + roundedScore);
        scoreText.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label dateText = new Label(isSeries ? "Airing: " + media.getProductionDate() : "Release: " + media.getProductionDate());
        dateText.setStyle("-fx-font-size: 12px;");

        VBox mediaCard = new VBox(10, posterImageView, titleText, scoreText, dateText);
        mediaCard.setStyle("-fx-alignment: center; -fx-padding: 10; -fx-background-color: #e0e0e0; -fx-border-radius: 10; -fx-background-radius: 10;");
        mediaCard.setPrefSize(180, 300);

        MediaCardUtil mediaCardUtil = new MediaCardUtil();
        mediaCardUtil.initializeMediaCardEvents(mediaCard, media, isSeries);

        return mediaCard;
    }

    public void onLoadingComplete() {
        loadingIndicator.setVisible(false);
    }

    public void loadMoreItems() {
        int end = Math.min(currentDisplayed + ITEMS_PER_PAGE, allMedia.size());
        displayMediaRange(currentDisplayed, end);
        currentDisplayed = end;
        loadMoreButton.setVisible(currentDisplayed < allMedia.size());
    }

    private void displayMediaRange(int start, int end) {
        Thread cardCreationThread = new Thread(() -> {
            List<VBox> mediaCards = new ArrayList<>();
            for (int i = start; i < end; i++) {
                Media media = allMedia.get(i);
                VBox mediaCard = createMediaCard(media, !isMovie);
                mediaCards.add(mediaCard);
            }
            
            Platform.runLater(() -> {
                for (int i = 0; i < mediaCards.size(); i++) {
                    int currentSize = mediaGrid.getChildren().size();
                    mediaGrid.add(mediaCards.get(i), currentSize % 4, currentSize / 4);
                }
                onLoadingComplete();
            });
        });
        cardCreationThread.start();
    }

    public FilterOptionsController getFilterOptionsController() {
        return filterOptionsController;
    }
}