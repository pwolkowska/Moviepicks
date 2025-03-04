package pl.edu.pw.mini.movieapp.models;

public class Series extends Media{

    private int seasons;

    public Series(int id, String title, String productionDate, double userScore, String posterPath) {
        super(id, title, productionDate, userScore, posterPath);
    }

    @Override
    public String formatMediaDetails() {
        return "Title: " + title + "\n" +
                "Overview: " + overview + "\n" +
                "Production date: " + productionDate + "\n" +
                "Production year: " + year + "\n" +
                "User Score: " + userScore + "\n" +
                "Popularity: " + popularity + "\n" +
                "Poster path: " + posterPath + "\n" +
                "Seasons: " + seasons + "\n" +
                "Country: " + country + "\n" +
                "Language: " + language;
    }

    public int getSeasons() {
        return seasons;
    }

    public void setSeasons(int seasons) {
        this.seasons = seasons;
    }
}