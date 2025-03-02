package pl.edu.pw.mini.movieapp.services;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TMDBClient {
    private static final String API_URL = "https://api.themoviedb.org/3";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI1MjMzYTBhN2QxODU4MWE5MmRiNGNkZDMzZTc1OTc0MiIsIm5iZiI6MTczNTc2NTAxNC42MDUsInN1YiI6IjY3NzVhYzE2ZGQxNzljNWIzNTkyNzM0MSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.rEhPpY3j8yw5XskGo039M0vEk12e4pfic8M74VqJNZ8"; // Wstaw swój klucz API
    private final OkHttpClient client;

    public TMDBClient() {
        this.client = new OkHttpClient();
    }

    public String sendRequest(String endpoint) throws Exception {
        String url = API_URL + endpoint;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer " + API_KEY)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                throw new RuntimeException("Błąd podczas żądania: " + response.code());
            }
        }
    }
}
