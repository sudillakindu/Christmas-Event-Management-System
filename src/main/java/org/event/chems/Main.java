package org.event.chems;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.util.Duration;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import static javafx.stage.StageStyle.UNDECORATED;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Parent loginForm = loadFXML("LoginForm.fxml");
        setApplicationIcon(primaryStage);
        Scene initialScene = new Scene(loginForm);
        primaryStage.setScene(initialScene);
        primaryStage.initStyle(UNDECORATED);
        primaryStage.setResizable(false);
        primaryStage.show();
        startFadeOutTransition(primaryStage, initialScene);
    }

    private Parent loadFXML(String fxml) {
        try {
            return FXMLLoader.load(Objects.requireNonNull(getClass().getResource(fxml)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setApplicationIcon(Stage primaryStage) {
        try (InputStream imageStream = getClass().getResourceAsStream("/images/image_title.png")) {
            primaryStage.getIcons().add(new Image(Objects.requireNonNull(imageStream)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startFadeOutTransition(Stage primaryStage, Scene initialScene) {
        try {
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), initialScene.getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> {
                Parent newLoginForm = loadFXML("LoginForm.fxml");
                //Parent newLoginForm = loadFXML("SystemForm.fxml");
                if (newLoginForm != null) {
                    primaryStage.setScene(new Scene(newLoginForm));
                    primaryStage.setTitle("Chems Login");
                    startFadeInTransition(newLoginForm);
                } else {
                    //showErrorDialog("Failed to load SystemForm.fxml.");
                }
            });
            fadeOut.play();
        } catch (Exception e) {
            // Log the error to the console
            System.err.println("Error loading FXML file.");
            //e.printStackTrace();
            //showErrorDialog("An error occurred while loading the FXML file.");
        }
    }

    private void startFadeInTransition(Parent root) {
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
