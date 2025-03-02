package pl.edu.pw.mini.movieapp.models;

import java.util.List;

abstract public class Media {
    protected int id;
    protected String title;
    protected String productionDate;
    protected int year;
    protected double userScore;
    protected String posterPath;
    protected String overview;
    protected double popularity;
    protected String country;
    protected String language;
    protected List<String> genres;
    protected boolean hasDetails;

    public Media(int id, String title, String productionDate, double userScore, String posterPath) {
        this.id = id;
        this.title = title;
        this.productionDate = productionDate;
        this.userScore = userScore;
        this.posterPath = posterPath;
        this.year = Integer.parseInt(productionDate.split("-")[0]);
    }   
    
    @Override
    public String toString() {
        return  "title='" + title + '\'' +
                ", year=" + year +
                ", userScore=" + userScore;
    }
    abstract public String formatMediaDetails();
    // getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getUserScore() {
        return userScore;
    }

    public void setUserScore(double userScore) {
        this.userScore = userScore;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public List<String> getGenres() {
        return genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public boolean isHasDetails() {
        return hasDetails;
    }

    public void setHasDetails(boolean hasDetails) {
        this.hasDetails = hasDetails;
    }

    public String getProductionDate() {
        return productionDate;
    }

    public void setProductionDate(String productionDate) {
        this.productionDate = productionDate;
    }
}