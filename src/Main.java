package com.chesserver;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("game-view.fxml")));
        Scene scene = new Scene(root, 800, 590);
        primaryStage.setScene(scene);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chess Game Online");
        primaryStage.setResizable(false);
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

    }
    public static void main(String[] args) {
        launch(args);
    }
}
