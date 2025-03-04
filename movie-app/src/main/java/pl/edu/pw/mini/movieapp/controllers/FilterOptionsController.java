package pl.edu.pw.mini.movieapp.controllers;

import java.util.List;
import java.util.stream.Collectors;
import org.controlsfx.control.RangeSlider;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import pl.edu.pw.mini.movieapp.models.Movie;
import pl.edu.pw.mini.movieapp.models.Series;
import pl.edu.pw.mini.movieapp.services.MovieService;
import pl.edu.pw.mini.movieapp.services.Order;
import pl.edu.pw.mini.movieapp.services.SeriesService;
import pl.edu.pw.mini.movieapp.services.TMDBClient;

public class FilterOptionsController {
    @FXML
    private ComboBox<String> sortOrderComboBox;
    
    @FXML
    private RangeSlider scoreRangeSlider;
    
    @FXML
    private RangeSlider yearRangeSlider;
    
    @FXML
    private Label minScoreLabel;
    
    @FXML
    private Label maxScoreLabel;
    
    @FXML
    private Label minYearLabel;
    
    @FXML
    private Label maxYearLabel;
    
    @FXML
    private Button applyButton;
    
    @FXML
    private Button resetButton;
    
    private MovieService movieService;
    private SeriesService seriesService;
    private MediaPageController mediaPageController;
    private String currentCategory;
    private boolean isMovie;
    private List<Movie> initialMovies;
    private List<Series> initialSeries;
    private List<VBox> initialMovieCards;
    private List<VBox> initialSeriesCards;
    @FXML
    private Label noResultsLabel;

    @FXML
    public void initialize() {
        TMDBClient tmdbClient = new TMDBClient();
        movieService = new MovieService(tmdbClient);
        seriesService = new SeriesService(tmdbClient);
        
        sortOrderComboBox.getItems().addAll(
            "Release Date (Newest)",
            "Release Date (Oldest)",
            "Title (A-Z)",
            "Title (Z-A)"
        );
        setupSliders();
        setupButtons();
    }

    private void setupSliders() {
        scoreRangeSlider.lowValueProperty().addListener((obs, oldVal, newVal) -> {
            minScoreLabel.setText(String.format("%.1f", newVal.doubleValue()));
        });
        
        scoreRangeSlider.highValueProperty().addListener((obs, oldVal, newVal) -> {
            maxScoreLabel.setText(String.format("%.1f", newVal.doubleValue()));
        });
        
        yearRangeSlider.lowValueProperty().addListener((obs, oldVal, newVal) -> {
            minYearLabel.setText(String.valueOf(newVal.intValue()));
        });
        
        yearRangeSlider.highValueProperty().addListener((obs, oldVal, newVal) -> {
            maxYearLabel.setText(String.valueOf(newVal.intValue()));
        });
    }

    private void setupButtons() {
        applyButton.setOnAction(event -> {
            applyFilters();
        });
        
        resetButton.setOnAction(event -> {
            noResultsLabel.setVisible(false);
            sortOrderComboBox.setValue(null);
            scoreRangeSlider.setLowValue(0.0);
            scoreRangeSlider.setHighValue(10.0);
            yearRangeSlider.setLowValue(1900);
            yearRangeSlider.setHighValue(java.time.Year.now().getValue());
            
            mediaPageController.loadingIndicator.setVisible(true);
            mediaPageController.getMediaGrid().setVisible(false);
            
            if (isMovie && initialMovies != null) {
                List<VBox> mediaCards = initialMovies.parallelStream()
                    .map(movie -> mediaPageController.createMediaCard(movie, false))
                    .collect(Collectors.toList());

                Platform.runLater(() -> {
                    mediaPageController.getMediaGrid().getChildren().clear();
                    for (int i = 0; i < mediaCards.size(); i++) {
                        mediaPageController.getMediaGrid().add(mediaCards.get(i), i % 4, i / 4);
                    }
                    mediaPageController.getMediaGrid().setVisible(true);
                    mediaPageController.onLoadingComplete();
                });

            } else if (!isMovie && initialSeries != null) {
                List<VBox> mediaCards = initialSeries.parallelStream()
                    .map(series -> mediaPageController.createMediaCard(series, true))
                    .collect(Collectors.toList());

                Platform.runLater(() -> {
                    mediaPageController.getMediaGrid().getChildren().clear();
                    for (int i = 0; i < mediaCards.size(); i++) {
                        mediaPageController.getMediaGrid().add(mediaCards.get(i), i % 4, i / 4);
                    }
                    mediaPageController.getMediaGrid().setVisible(true);
                    mediaPageController.onLoadingComplete();
                });
            }
        });
    }

    public void setMediaPageController(MediaPageController controller) {
        this.mediaPageController = controller;
    }

    public void setCurrentCategory(String category, boolean isMovie) {
        if (!category.equals(this.currentCategory) || this.isMovie != isMovie) {
            initialMovies = null;
            initialSeries = null;
            initialMovieCards = null;
            initialSeriesCards = null;
            this.currentCategory = category;
            this.isMovie = isMovie;
        }
    }

    private Order getSelectedOrder() {
        String selected = sortOrderComboBox.getValue();
        if (selected == null) return null;
        
        return switch (selected) {
            case "Release Date (Newest)" -> Order.DATE_DESC;
            case "Release Date (Oldest)" -> Order.DATE_ASC;
            case "Title (A-Z)" -> Order.TITLE_ASC;
            case "Title (Z-A)" -> Order.TITLE_DESC;
            default -> null;
        };
    }

    public void applyFilters() {
        if (mediaPageController == null || currentCategory == null) return;
        
        // Show loading indicator before starting the thread
        mediaPageController.loadingIndicator.setVisible(true);
        mediaPageController.getMediaGrid().setVisible(false);
        noResultsLabel.setVisible(false);


        Thread processThread = new Thread(() -> {
            try {
                // Add small delay to ensure loading indicator is visible
                Thread.sleep(500);
                
                if (isMovie) {
                    if (initialMovies == null) {
                        initialMovies = switch (currentCategory) {
                            case "Popular" -> movieService.getPopularMovies();
                            case "Now Playing" -> movieService.getNowPlayingMovies();
                            case "Top Rated" -> movieService.getTopRatedMovies();
                            case "Upcoming" -> movieService.getUpcomingMovies();
                            default -> movieService.getPopularMovies();
                        };
                    }
                    // Apply filters
                    List<Movie> movies = movieService.filterMovies(initialMovies, 
                        scoreRangeSlider.getLowValue(), scoreRangeSlider.getHighValue(), 
                        (int)yearRangeSlider.getLowValue(), (int)yearRangeSlider.getHighValue());
                    
                    // Apply sorting if selected
                    Order selectedOrder = getSelectedOrder();
                    if (selectedOrder != null) {
                        movies = movieService.sortMovies(movies, selectedOrder);
                    }
                    
                    final List<Movie> filteredMovies = movies;
                    Platform.runLater(() -> {
                        mediaPageController.getMediaGrid().getChildren().clear();
                        mediaPageController.allMedia = filteredMovies;
                        mediaPageController.currentDisplayed = 0;
                        mediaPageController.loadMoreItems();
                        mediaPageController.getMediaGrid().setVisible(true);
                        if (filteredMovies.isEmpty()) {
                            noResultsLabel.setVisible(true);
                        }
                    });
                } else {
                    if (initialSeries == null) {
                        initialSeries = switch (currentCategory) {
                            case "Popular" -> seriesService.getPopularSeries();
                            case "Top Rated" -> seriesService.getTopRatedSeries();
                            case "Airing Today" -> seriesService.getAiringTodaySeries();
                            default -> seriesService.getPopularSeries();
                        };
                    }
                    List<Series> series = seriesService.filterSeries(initialSeries,
                        scoreRangeSlider.getLowValue(), scoreRangeSlider.getHighValue(),
                        (int)yearRangeSlider.getLowValue(), (int)yearRangeSlider.getHighValue());
                    
                    // Apply sorting if selected
                    Order selectedOrder = getSelectedOrder();
                    if (selectedOrder != null) {
                        series = seriesService.sortSeries(series, selectedOrder);
                    }
                        
                    final List<Series> filteredSeries = series;
                    Platform.runLater(() -> {
                        mediaPageController.getMediaGrid().getChildren().clear();
                        mediaPageController.allMedia = filteredSeries;
                        mediaPageController.currentDisplayed = 0;
                        mediaPageController.loadMoreItems();
                        mediaPageController.getMediaGrid().setVisible(true);
                    });
                    if (filteredSeries.isEmpty()) {
                        noResultsLabel.setVisible(true);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        processThread.start();
    }
}
