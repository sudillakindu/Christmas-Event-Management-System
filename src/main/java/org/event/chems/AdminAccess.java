package org.event.chems;

import javafx.scene.control.Alert;
import javafx.scene.layout.Region;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.io.IOException;
import java.util.Objects;

import static javafx.stage.StageStyle.UNDECORATED;

public class AdminAccess extends JFrame {

    private JTextField userText;
    private JPasswordField passwordText;
    private SystemForm systemForm;

    public AdminAccess(SystemForm systemForm) {
        this.systemForm = systemForm;
        initializeLoginPage();
    }

    private void initializeLoginPage() {
        setTitle("Admin Login Page");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        try (InputStream imageStream = getClass().getResourceAsStream("/images/image_login2.png")) {
            if (imageStream != null) {
                setIconImage(ImageIO.read(imageStream));
            } else {
                System.err.println("Image not found.");
                showAlert("Image not found.", Alert.AlertType.ERROR);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading icon image.", "Error", JOptionPane.ERROR_MESSAGE);
        }

        JPanel panel = new JPanel(null);
        panel.setBackground(new Color(204, 229, 255));
        initializeComponents(panel);
        add(panel);
        setVisible(true);
    }

    private void initializeComponents(JPanel panel) {
        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(50, 50, 100, 30);
        panel.add(userLabel);

        userText = new JTextField();
        userText.setBounds(150, 50, 200, 30);
        panel.add(userText);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 100, 100, 30);
        panel.add(passwordLabel);

        passwordText = new JPasswordField();
        passwordText.setBounds(150, 100, 200, 30);
        panel.add(passwordText);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(150, 150, 100, 30);
        loginButton.setBackground(new Color(51, 153, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.addActionListener(e -> handleLogin());
        panel.add(loginButton);
    }

    private void handleLogin() {
        String username = userText.getText().trim();
        String password = new String(passwordText.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username and Password cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (isLoginSuccessful(username, password)) {
            systemForm.disableAdminPanel(false);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Username or Password!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isLoginSuccessful(String username, String password) {
        return Objects.equals(username, "admin") && Objects.equals(password, "Admin@159");
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.initStyle(UNDECORATED);
        alert.setHeaderText(null);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.getDialogPane().setStyle("-fx-background-color: white;" +
                "-fx-border-color: #666464; -fx-border-width: 3; -fx-font-family: 'Arial'; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment: center; -fx-padding: 10;");
        alert.setContentText(message);
        alert.showAndWait();
    }

}
