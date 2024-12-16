package org.event.chems;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.*;
import java.io.IOException;
import java.util.Objects;

public class LoginForm {

    @FXML
    public TextField usernameTextField, passwordTextField;
    @FXML
    public TextField errorFieldWelcome, errorFieldSuccessful, errorFieldInvalid, errorFieldRequired;
    @FXML
    private PasswordField hiddenPasswordTextField;
    @FXML
    private CheckBox showPassword;
    @FXML
    private Button loginButton;
    @FXML
    private ImageView loginFormImage, loginFormLogo;

    private static final String WELCOME_STATUS_YES = "yes";
    private static final String WELCOME_STATUS_NO = "no";

    private String welcomeStatus = WELCOME_STATUS_YES;
    private final PauseTransition pauseField = new PauseTransition(Duration.millis(1500));

    public void initialize() {
        loginFormImage.setImage(new Image(getClass().getResourceAsStream("/images/image_login_background.png")));
        loginFormLogo.setImage(new Image(getClass().getResourceAsStream("/images/logo.png")));
    }

    @FXML
    void changeVisibility(ActionEvent event) {
        boolean isPasswordVisible = showPassword.isSelected();
        passwordTextField.setVisible(isPasswordVisible);
        hiddenPasswordTextField.setVisible(!isPasswordVisible);

        if (isPasswordVisible) {
            passwordTextField.setText(hiddenPasswordTextField.getText());
        } else {
            hiddenPasswordTextField.setText(passwordTextField.getText());
        }
    }

    @FXML
    void loginHandler() {
        resetErrorFields();

        String username = usernameTextField.getText().trim();
        String password = getPassword().trim();

        if (welcomeStatus.equals(WELCOME_STATUS_YES)) {
            errorFieldWelcome.setVisible(false);
            welcomeStatus = WELCOME_STATUS_NO;
        }

        if (!validateInputs(username, password)) return;

        if (isLoginSuccessful(username, password)) {
            handleSuccessfulLogin();
        } else {
            handleInvalidLogin();
        }
    }

    private void resetErrorFields() {
        errorFieldRequired.setVisible(false);
        errorFieldInvalid.setVisible(false);
        errorFieldSuccessful.setVisible(false);
    }

    @FXML
    void exitApplication(ActionEvent event) {
        Platform.exit();
    }

    private boolean validateInputs(String username, String password) {
        if (username.isEmpty()) {
            setErrorField(errorFieldRequired, "Username is required!");
            return false;
        }
        if (password.isEmpty()) {
            setErrorField(errorFieldRequired, "Password is required!");
            return false;
        }
        return true;
    }

    private boolean isLoginSuccessful(String username, String password) {
        return Objects.equals(username, "user") && Objects.equals(password, "user@57");
    }

    private void handleSuccessfulLogin() {
        errorFieldSuccessful.setVisible(true);
        pauseField.setOnFinished(e -> {
            errorFieldSuccessful.setVisible(false);
            loadSystemForm();
        });
        pauseField.play();
    }

    private void handleInvalidLogin() {
        errorFieldInvalid.setVisible(true);
        pauseField.setOnFinished(e -> errorFieldInvalid.setVisible(false));
        pauseField.play();
    }

    private void setErrorField(TextField errorField, String message) {
        errorField.setText(message);
        errorField.setVisible(true);
        pauseField.setOnFinished(e -> errorField.setVisible(false));
        pauseField.play();
    }

    private String getPassword() {
        return passwordTextField.isVisible() ? passwordTextField.getText() : hiddenPasswordTextField.getText();
    }

    private void loadSystemForm() {
        try {
            Parent systemForm = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("SystemForm.fxml")));
            Stage stage = (Stage) loginButton.getScene().getWindow();

            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), stage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(event -> {
                stage.setScene(new Scene(systemForm));
                stage.setTitle("Chems System");
                FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), systemForm);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
        } catch (IOException e) {
            System.err.println("Unable to load the system form.\nPlease check your database connection and try again.\nError details: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Unable to load the system form.\nPlease check your database connection and try again.\nError details: " + e.getMessage(),
                    "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
