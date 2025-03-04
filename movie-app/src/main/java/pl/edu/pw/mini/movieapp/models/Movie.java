package pl.edu.pw.mini.movieapp.models;

public class Movie extends Media{
    // detailed movie data
    private int length;

    public Movie(int id, String title, String productionDate, double userScore, String posterPath) {
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
                "Length: " + length + " minutes\n" +
                "Country: " + country + "\n" +
                "Language: " + language;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }
}