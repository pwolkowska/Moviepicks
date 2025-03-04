package pl.edu.pw.mini.movieapp.services;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pl.edu.pw.mini.movieapp.models.Series;

public class SeriesService{
    private final TMDBClient tmdbClient;
  

    public SeriesService(TMDBClient tmdbClient) {
        this.tmdbClient = tmdbClient;
    }

    public List<Series> getPopularSeries() {
        return getSeries("popular", false);
    }

    public List<Series> getAiringTodaySeries() {
        return getSeries("airing_today", false);
    }

    public List<Series> getTopRatedSeries() {
        return getSeries("top_rated", false);
    }

    public List<Series> getOnTheAirSeries() {
        return getSeries("on_the_air", false);
    }

    public List<Series> sortSeries(List<Series> series, Order order) {
        List<Series> sortedSeries = new ArrayList<>(series);
        
        switch (order) {
            case DATE_DESC:
                sortedSeries.sort((s1, s2) -> {
                    if (s1.getProductionDate().equals("Unknown Release Date")) return 1;
                    if (s2.getProductionDate().equals("Unknown Release Date")) return -1;
                    return s2.getProductionDate().compareTo(s1.getProductionDate());
                });
                break;
                
            case DATE_ASC:
                sortedSeries.sort((s1, s2) -> {
                    if (s1.getProductionDate().equals("Unknown Release Date")) return 1;
                    if (s2.getProductionDate().equals("Unknown Release Date")) return -1;
                    return s1.getProductionDate().compareTo(s2.getProductionDate());
                });
                break;
                
            case TITLE_ASC:
                sortedSeries.sort((s1, s2) -> s1.getTitle().compareToIgnoreCase(s2.getTitle()));
                break;
                
            case TITLE_DESC:
                sortedSeries.sort((s1, s2) -> s2.getTitle().compareToIgnoreCase(s1.getTitle()));
                break;
        }
        
        return sortedSeries;
    }

    public List<Series> filterSeries(List<Series> series, Double minScore, Double maxScore, 
                                Integer fromYear, Integer toYear) {
        return series.stream()
            .filter(show -> {
                boolean matchesScore = true;
                boolean matchesYear = true;
                
                // Score filter
                if (minScore != null && maxScore != null) {
                    matchesScore = show.getUserScore() >= minScore && show.getUserScore() <= maxScore;
                }
                
                // Year filter
                if (fromYear != null && toYear != null && !show.getProductionDate().equals("Unknown Release Date")) {
                    int showYear = Integer.parseInt(show.getProductionDate().substring(0, 4));
                    matchesYear = showYear >= fromYear && showYear <= toYear;
                }
                
                return matchesScore && matchesYear;
            })
            .collect(Collectors.toList());
    }

    public List<Series> getSeries(String typeOrQuery, boolean isSearch) {
        List<Series> series = new ArrayList<>();
        String endpoint;

        if (isSearch) {
            endpoint = "/search/tv?query=" + typeOrQuery;
        } else {
            endpoint = "/tv/" + typeOrQuery + "?page=1"; // Start with page 1
        }

        try {
            int currentPage = 1;
            int totalPages;

            do {
                String response = tmdbClient.sendRequest(endpoint + "&page=" + currentPage);
                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                totalPages = Math.min(jsonObject.get("total_pages").getAsInt(), 5); // Limit to 5 pages
                JsonArray results = jsonObject.getAsJsonArray("results");

                for (int i = 0; i < results.size(); i++) {
                    JsonObject seriesJson = results.get(i).getAsJsonObject();
                    String title = seriesJson.get("name").getAsString();
                    int id = seriesJson.get("id").getAsInt();
                    String productionDate = seriesJson.has("first_air_date") && !seriesJson.get("first_air_date").isJsonNull() 
                        ? seriesJson.get("first_air_date").getAsString() 
                        : "Unknown Release Date"; // Default value if null
                    double userScore = seriesJson.has("vote_average") && !seriesJson.get("vote_average").isJsonNull() 
                        ? seriesJson.get("vote_average").getAsDouble() 
                        : 0.0; // Default value if null
                    String posterPath = seriesJson.has("poster_path") && !seriesJson.get("poster_path").isJsonNull() 
                        ? "https://image.tmdb.org/t/p/w500" + seriesJson.get("poster_path").getAsString() 
                        : null;

                    series.add(new Series(id, title, productionDate, userScore, posterPath));
                }
                currentPage++;
            } while (currentPage <= totalPages);
        } catch (Exception e) {
            System.err.println("Error fetching series: " + e.getMessage());
        }
        return series;
    }
    
    public void getSeriesDetails (Series series) {
        if (!series.isHasDetails()) {
            try {
                String endpoint = "/tv/" + series.getId();
                String response = tmdbClient.sendRequest(endpoint);
                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                series.setOverview(jsonObject.get("overview").getAsString());
                series.setSeasons(jsonObject.get("number_of_seasons").getAsInt());
                series.setPopularity(jsonObject.get("popularity").getAsDouble());
                series.setCountry(jsonObject.getAsJsonArray("production_countries").get(0).getAsJsonObject().get("name").getAsString());
                series.setLanguage(jsonObject.get("original_language").getAsString().toUpperCase());
                JsonArray genresArray = jsonObject.getAsJsonArray("genres");
                List<String> genres = new ArrayList<>();
                for (int i = 0; i < genresArray.size(); i++) {
                    JsonObject genreObject = genresArray.get(i).getAsJsonObject();
                    String genreName = genreObject.get("name").getAsString();
                    genres.add(genreName);
                }
                series.setGenres(genres);
                series.setHasDetails(true);
            } catch (Exception e) {
                System.err.println("Błąd podczas pobierania szczegółów serialu: " + e.getMessage());
            }
        }
    }

    public List<Series> getTrendingTodaySeries() {
        List<Series> series = new ArrayList<>();
        String typeOrQuery = "tv"; // Specify the type for series
        String endpoint = "/trending/" + typeOrQuery + "/day"; // Endpoint for trending series

        try {
            String response = tmdbClient.sendRequest(endpoint);
            JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();

            JsonArray results = jsonObject.getAsJsonArray("results");

            for (int i = 0; i < results.size(); i++) {
                JsonObject seriesJson = results.get(i).getAsJsonObject();
                String title = seriesJson.get("name").getAsString();
                int id = seriesJson.get("id").getAsInt();
                String productionDate = seriesJson.get("first_air_date").getAsString();
                double userScore = seriesJson.get("vote_average").getAsDouble();
                String posterPath = seriesJson.has("poster_path") ? "https://image.tmdb.org/t/p/w500" + seriesJson.get("poster_path").getAsString() : null;
                series.add(new Series(id, title, productionDate, userScore, posterPath));
            }
        } catch (Exception e) {
            System.err.println("Error fetching trending series: " + e.getMessage());
        }
        return series;
    }

    public List<Series> getSimilarSeries(Series series){
        List<Series> ser = new ArrayList<>();
        String endpoint = "/tv/"+series.getId()+"/similar";
        try {
            // Dodajemy logowanie endpointu
            String response = tmdbClient.sendRequest(endpoint);
            JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
            JsonArray results = jsonObject.getAsJsonArray("results");
            int max = results.size()>=4?4:results.size();
            for (int i=0; i< max;i++){
                JsonObject seriesJson = results.get(i).getAsJsonObject();
                String title = seriesJson.get("name").getAsString();
                int id = seriesJson.get("id").getAsInt();
                String productionDate = seriesJson.get("first_air_date").getAsString();
                double userScore = seriesJson.get("vote_average").getAsDouble();
                String posterPath = seriesJson.has("poster_path") ?
                        "https://image.tmdb.org/t/p/w500" + seriesJson.get("poster_path").getAsString() : null;
                ser.add(new Series(id, title, productionDate, userScore, posterPath));
            }

        } catch (Exception e) {
            System.err.println("Error fetching similar movies: " + e.getMessage());
        }
        return ser;
    }

    public String getSeriesTrailer(Series series){
        String link ="";
        String endpoint = "/tv/"+series.getId()+"/videos";
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