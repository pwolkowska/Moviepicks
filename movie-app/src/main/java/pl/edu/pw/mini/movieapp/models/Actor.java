package pl.edu.pw.mini.movieapp.models;

import java.util.List;

public class Actor {
    private int id;
    private String name;
    private String biography;
    private double popularity;
    private String profilePath;
    private List<Media> knownFor;
    private boolean hasDetails;

    public Actor(int id, String name, double popularity,String profilePath) {
        this.id = id;
        this.name = name;
        this.popularity = popularity;
        this.profilePath=profilePath;
    }

    @Override
    public String toString() {
        return "Actor:" +
                name +
                ", popularity=" + popularity;
    }
    public String formatActorDetails(){
        String s = "Actor: "+name+"\n"
                +"Biography: "+biography+"\n"+
                "Films: "+"\n";
        for (Media media: knownFor){
            s = s + media.title +", "+media.year+"\n";
        }
        return s;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public boolean isHasDetails() {
        return hasDetails;
    }

    public void setHasDetails(boolean hasDetails) {
        this.hasDetails = hasDetails;
    }

    public String getProfilePath() {
        return profilePath;
    }

    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }

    public List<Media> getKnownFor() {
        return knownFor;
    }

    public void setKnownFor(List<Media> knownFor) {
        this.knownFor = knownFor;
    }
}