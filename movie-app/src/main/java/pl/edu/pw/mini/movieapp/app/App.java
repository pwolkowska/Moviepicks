package pl.edu.pw.mini.movieapp.app;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ScrollPane;
import javafx.stage.Stage;

public class App extends Application {

    private static Scene mainScene;
    
     public static void main(String[] args) {
        // Ignoring the error
        System.setErr(new java.io.PrintStream(new java.io.OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        }));
        launch(args); // Launching the application
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        if (!isInternetAvailable()) {
            Platform.runLater(() -> showAlert("No Internet Connection", "Please check your internet connection and try again."));
            return;
        }
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
        ScrollPane root = loader.load();

        // Set the scene with the root element
        mainScene = new Scene(root, 1100, 700);
        mainScene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        // Set the stage (window)
        primaryStage.setTitle("Moviepicks");
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    private static boolean isInternetAvailable() {
        try {
            URI uri = new URI("http://www.google.com");
            HttpURLConnection urlConnection = (HttpURLConnection) uri.toURL().openConnection();
            urlConnection.setRequestProperty("User-Agent", "test");
            urlConnection.setRequestProperty("Connection", "close");
            urlConnection.setConnectTimeout(1000); // Timeout in milliseconds
            urlConnection.connect();
            return (urlConnection.getResponseCode() == 200);
        } catch (Exception e) {
            return false;
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
        Platform.exit(); // Exit the application if no internet
    }

    public static Scene getMainScene() {
        return mainScene;
    }
}
