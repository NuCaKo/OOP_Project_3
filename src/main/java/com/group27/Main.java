package com.group27;

import com.group27.core.DatabaseAdapter;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
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

    public static void main(String[] args) {
        // macOS için JVM argümanları
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name", "GreenGrocer");
        
        launch();
    }
}