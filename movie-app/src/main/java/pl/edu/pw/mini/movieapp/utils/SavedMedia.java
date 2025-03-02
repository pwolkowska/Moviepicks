package pl.edu.pw.mini.movieapp.utils;

import pl.edu.pw.mini.movieapp.models.Media;

import java.util.HashMap;
import java.util.Map;

public class SavedMedia {
    private static final SavedMedia instance = new SavedMedia(); // Singleton

    private final Map<Integer, Media> savedMedia = new HashMap<>();

    private SavedMedia() {}

    public static SavedMedia getInstance() {
        return instance;
    }

    public Map<Integer, Media> getSavedMedia() {
        return savedMedia;
    }

    public boolean isSaved(Media media) {
        return savedMedia.containsKey(media.getId());
    }

    public void saveMedia(Media media) {
        savedMedia.put(media.getId(), media);
    }

    public void deleteMedia(Media media) {
        savedMedia.remove(media.getId());
    }
}
