package pl.edu.pw.mini.movieapp.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import pl.edu.pw.mini.movieapp.models.Actor;
import pl.edu.pw.mini.movieapp.models.Media;
import pl.edu.pw.mini.movieapp.models.Movie;
import pl.edu.pw.mini.movieapp.models.Series;

import java.util.ArrayList;
import java.util.List;

public class ActorService {
    private final TMDBClient tmdbClient;

    public ActorService(TMDBClient tmdbClient) {
        this.tmdbClient = tmdbClient;
    }

    public List<Actor> searchActors(String query,boolean isSearch) {
        List<Actor> actors = new ArrayList<>();
        String endpoint = "/search/person?query=" + query;

        try {
            String response = tmdbClient.sendRequest(endpoint);
            JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
            JsonArray results = jsonObject.getAsJsonArray("results");

            for (int i = 0; i < results.size(); i++) {
                JsonObject actorJson = results.get(i).getAsJsonObject();
                String name = actorJson.get("name").getAsString();
                int id = actorJson.get("id").getAsInt();
                double popularity = actorJson.get("popularity").getAsDouble();
                String profilePath = actorJson.has("profile_path") && !actorJson.get("profile_path").isJsonNull()
                        ? "https://image.tmdb.org/t/p/w500" + actorJson.get("profile_path").getAsString()
                        : "No profile";
                actors.add(new Actor(id, name, popularity,profilePath));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return actors;
    }

    public void getActorDetails(Actor actor) throws Exception {
        if (!actor.isHasDetails()){
            try{
                String endpoint = "/search/person?query=" + actor.getName();
                String response = tmdbClient.sendRequest(endpoint);
                JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
                JsonArray results = jsonObject.getAsJsonArray("results");
                if (results.size()>0) {
                    JsonObject actorJson = results.get(0).getAsJsonObject();

                    JsonArray mediaArray = actorJson.has("known_for") && !actorJson.get("known_for").isJsonNull()
                            ? actorJson.getAsJsonArray("known_for")
                            : new JsonArray();
                    List<Media> media = new ArrayList<>();
                    for (int i = 0; i < mediaArray.size(); i++) {
                        JsonObject mediaObject = mediaArray.get(i).getAsJsonObject();
                        int id = mediaObject.get("id").getAsInt();
                        String title = mediaObject.get("title").getAsString();
                        String productionDate = mediaObject.get("release_date").getAsString();
                        double userScore = mediaObject.get("vote_average").getAsDouble();
                        String posterPath = mediaObject.has("poster_path") && !mediaObject.get("poster_path").isJsonNull()
                                ? "https://image.tmdb.org/t/p/w500" + mediaObject.get("poster_path").getAsString()
                                : null;
                        if (mediaObject.get("media_type").getAsString().equals("movie")) {
                            media.add(new Movie(id, title, productionDate, userScore, posterPath));
                        } else if (mediaObject.get("media_type").getAsString().equals("tv")) {
                            media.add(new Series(id, title, productionDate, userScore, posterPath));
                        }
                    }
                    actor.setKnownFor(media);
                    String endpoint2 = "/person/" + actor.getId();
                    String response2 = tmdbClient.sendRequest(endpoint2);
                    JsonObject jsonObject2 = JsonParser.parseString(response2).getAsJsonObject();
                    String biography = jsonObject2.has("biography") && !jsonObject2.get("biography").isJsonNull()
                            ? jsonObject2.get("biography").getAsString() : "No biography available";
                    actor.setBiography(biography);
                    actor.setHasDetails(true);
                }
            } catch (Exception e) {
                System.err.println("Błąd podczas pobierania danych o aktorze: " + e.getMessage());
            }
        }
    }
}