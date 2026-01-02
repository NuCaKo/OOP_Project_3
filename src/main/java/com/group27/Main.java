package com.group27;

import com.group27.core.DatabaseAdapter;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application class for the GreenGrocer application.
 * This class extends JavaFX Application and serves as the entry point
 * for the GUI application. It initializes the database connection and
 * loads the login screen.
 * 
 * @author Group27
 * @version 1.0
 */
public class Main extends Application {
    /**
     * Initializes and displays the login window.
     * This method is called by JavaFX when the application starts.
     * It initializes the database adapter and loads the login FXML scene.
     * 
     * @param stage The primary stage for the application
     * @throws IOException If the FXML file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        // Initialize DB
        DatabaseAdapter.getInstance();
        
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/fxml/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 960, 540);
        stage.setTitle("Group27 GreenGrocer - Login");
        stage.setScene(scene);
        stage.setResizable(false); // macOS uyumluluk için
        stage.centerOnScreen();
        stage.show();
    }

    /**
     * Main entry point for the application.
     * Sets macOS-specific system properties for better integration
     * and launches the JavaFX application.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // macOS için JVM argümanları
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "GreenGrocer");
        
        launch();
    }
}