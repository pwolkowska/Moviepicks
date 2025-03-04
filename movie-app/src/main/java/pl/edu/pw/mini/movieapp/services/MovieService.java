package pl.edu.pw.mini.movieapp.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pl.edu.pw.mini.movieapp.models.Movie;

public class MovieService {

    private final TMDBClient tmdbClient;

    public MovieService(TMDBClient tmdbClient) {
        this.tmdbClient = tmdbClient;
    }

    // Search Movies
    public List<Movie> searchMovies(String query) {
        return getMovies(query, true);
    }

    // Popular Movies
    public List<Movie> getPopularMovies() {
        return getMovies("popular", false);
    }

    // Now Playing Movies
    public List<Movie> getNowPlayingMovies() {
        return getMovies("now_playing", false);
    }

    // Top Rated Movies
    public List<Movie> getTopRatedMovies() {
        return getMovies("top_rated", false);
    }

    // Upcoming Movies
    public List<Movie> getUpcomingMovies() {
        return getMovies("upcoming", false);
    }

    public List<Movie> sortMovies(List<Movie> movies, Order order) {
        List<Movie> sortedMovies = new ArrayList<>(movies);
        
        switch (order) {
            case DATE_DESC:
                sortedMovies.sort((m1, m2) -> {
                    if (m1.getProductionDate().equals("Unknown Release Date")) return 1;
                    if (m2.getProductionDate().equals("Unknown Release Date")) return -1;
                    return m2.getProductionDate().compareTo(m1.getProductionDate());
                });
                break;
                
            case DATE_ASC:
                sortedMovies.sort((m1, m2) -> {
                    if (m1.getProductionDate().equals("Unknown Release Date")) return 1;
                    if (m2.getProductionDate().equals("Unknown Release Date")) return -1;
                    return m1.getProductionDate().compareTo(m2.getProductionDate());
                });
                break;
                
            case TITLE_ASC:
                sortedMovies.sort((m1, m2) -> m1.getTitle().compareToIgnoreCase(m2.getTitle()));
                break;
                
            case TITLE_DESC:
                sortedMovies.sort((m1, m2) -> m2.getTitle().compareToIgnoreCase(m1.getTitle()));
                break;
        }
        
        return sortedMovies;
    }

    public List<Movie> filterMovies(List<Movie> movies, Double minScore, Double maxScore, 
                               Integer fromYear, Integer toYear) {
        return movies.stream()
            .filter(movie -> {
                boolean matchesScore = true;
                boolean matchesYear = true;
                
                // Score filter
                if (minScore != null && maxScore != null) {
                    matchesScore = movie.getUserScore() >= minScore && movie.getUserScore() <= maxScore;
                }
                
                // Year filter
                if (fromYear != null && toYear != null && !movie.getProductionDate().equals("Unknown Release Date")) {
                    int movieYear = Integer.parseInt(movie.getProductionDate().substring(0, 4));
                    matchesYear = movieYear >= fromYear && movieYear <= toYear;
                }
                
                return matchesScore && matchesYear;
            })
            .collect(Collectors.toList());
    }

    public List<Movie> getMovies(String typeOrQuery, boolean isSearch) {
        List<Movie> movies = new ArrayList<>();
        String endpoint;
    
        if (isSearch) {
            endpoint = "/search/movie?query=" + typeOrQuery;
        } else {
            endpoint = "/movie/" + typeOrQuery + "?page=1";
        }

        try {
            int currentPage = 1;
            int totalPages;
    
            do {
                String response = tmdbClient.sendRequest(endpoint + "&page=" + currentPage);
                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                totalPages = jsonObject.get("total_pages").getAsInt() > 5 ? 5 : jsonObject.get("total_pages").getAsInt();
                JsonArray results = jsonObject.getAsJsonArray("results");
    
                for (int j = 0; j < results.size(); j++) {
                    JsonObject movieJson = results.get(j).getAsJsonObject();
                    String title = movieJson.get("title").getAsString();
                    int id = movieJson.get("id").getAsInt();
                    
                    // Handle production date
                    String productionDate = movieJson.has("release_date") && !movieJson.get("release_date").isJsonNull() 
                        ? movieJson.get("release_date").getAsString() 
                        : "Unknown Release Date"; // Default value if null
    
                    // Handle user score
                    double userScore = 0.0; // Default value
                    if (movieJson.has("vote_average") && !movieJson.get("vote_average").isJsonNull()) {
                        String voteAverageStr = movieJson.get("vote_average").getAsString();
                        if (!voteAverageStr.isEmpty()) {
                            try {
                                userScore = Double.parseDouble(voteAverageStr); // Use parseDouble to handle empty strings
                            } catch (NumberFormatException e) {
                                System.err.println("Error parsing vote_average: " + voteAverageStr);
                            }
                        }
                    }
    
                    // Handle poster path
                    String posterPath = movieJson.has("poster_path") && !movieJson.get("poster_path").isJsonNull() 
                        ? "https://image.tmdb.org/t/p/w500" + movieJson.get("poster_path").getAsString() 
                        : null;
    
                    movies.add(new Movie(id, title, productionDate, userScore, posterPath));
                }
                currentPage++;
            } while (currentPage <= totalPages);
        } catch (Exception e) {
            System.err.println("Error fetching movies: " + e.getMessage());
        }
        return movies;
    }

    // Load the details of a selected movie
    public void getMovieDetails (Movie movie) {
        if (!movie.isHasDetails()) {
            try {
                String endpoint = "/movie/" + movie.getId();
                String response = tmdbClient.sendRequest(endpoint);
                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                movie.setOverview(jsonObject.get("overview").getAsString());
                movie.setLength(jsonObject.get("runtime").getAsInt());
                movie.setPopularity(jsonObject.get("popularity").getAsDouble());
                movie.setCountry(jsonObject.getAsJsonArray("production_countries").get(0).getAsJsonObject().get("name").getAsString());
                movie.setLanguage(jsonObject.get("original_language").getAsString().toUpperCase());
                JsonArray genresArray = jsonObject.getAsJsonArray("genres");
                List<String> genres = new ArrayList<>();
                for (int i = 0; i < genresArray.size(); i++) {
                    JsonObject genreObject = genresArray.get(i).getAsJsonObject();
                    String genreName = genreObject.get("name").getAsString();
                    genres.add(genreName);
                }
                movie.setGenres(genres);
                movie.setHasDetails(true);
            } catch (Exception e) {
                System.err.println("Błąd podczas pobierania szczegółów filmu: " + e.getMessage());
            }
        }
    }

    public List<Movie> getTrendingTodayMovies() {
        List<Movie> movies = new ArrayList<>();
        String endpoint = "/trending/movie/day";

        try {
            String response = tmdbClient.sendRequest(endpoint);
            JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
            JsonArray results = jsonObject.getAsJsonArray("results");
            for (int i = 0; i < results.size(); i++) {
                JsonObject movieJson = results.get(i).getAsJsonObject();
                String title = movieJson.get("title").getAsString();
                int id = movieJson.get("id").getAsInt();
                String productionDate = movieJson.get("release_date").getAsString();
                double userScore = movieJson.get("vote_average").getAsDouble();
                String posterPath = movieJson.has("poster_path") ?
                        "https://image.tmdb.org/t/p/w500" + movieJson.get("poster_path").getAsString() : null;
                movies.add(new Movie(id, title, productionDate, userScore, posterPath));
            }
        } catch (Exception e) {
            System.err.println("Error fetching trending movies: " + e.getMessage());
        }

        return movies;
    }

    public List<Movie> getSimilarMovies(Movie movie){
        List<Movie> movies = new ArrayList<>();
        String endpoint = "/movie/"+movie.getId()+"/similar";
        try {
            String response = tmdbClient.sendRequest(endpoint);
            JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
            JsonArray results = jsonObject.getAsJsonArray("results");
            int max = results.size()>=4?4:results.size();
            for (int i=0; i< max;i++){
                JsonObject movieJson = results.get(i).getAsJsonObject();
                String title = movieJson.get("title").getAsString();
                int id = movieJson.get("id").getAsInt();
                String productionDate = movieJson.get("release_date").getAsString();
                double userScore = movieJson.get("vote_average").getAsDouble();
                String posterPath = movieJson.has("poster_path") ?
                        "https://image.tmdb.org/t/p/w500" + movieJson.get("poster_path").getAsString() : null;
                movies.add(new Movie(id, title, productionDate, userScore, posterPath));
            }

        } catch (Exception e) {
            System.err.println("Error fetching similar movies: " + e.getMessage());
        }
        return movies;
    }

    public String getMovieTrailer(Movie movie){
        String link ="";
        String endpoint = "/movie/"+movie.getId()+"/videos";
        try {
            String response = tmdbClient.sendRequest(endpoint);
            JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
            JsonArray results = jsonObject.getAsJsonArray("results");
            for (int i=0; i< results.size();i++){
                JsonObject trailer = results.get(i).getAsJsonObject();
                if (trailer.get("official").getAsBoolean()==true && trailer.get("type").getAsString().equals("Trailer") && trailer.get("site").getAsString().equals("YouTube")){
                    link = "https://www.youtube.com/watch?v="+trailer.get("key").getAsString();
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching trailer: " + e.getMessage());
        }
        return link;
    }
}