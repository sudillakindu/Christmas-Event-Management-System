package org.event.chems;

import javafx.scene.control.Alert;
import javafx.scene.layout.Region;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static javafx.stage.StageStyle.UNDECORATED;

public class Encryptor {

    public String encryptString(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger bigInt = new BigInteger(1, messageDigest);
            return bigInt.toString(16);
        } catch (NoSuchAlgorithmException e) {
            System.err.println("MD5 algorithm not found.");
            showAlert("MD5 algorithm not found.", Alert.AlertType.ERROR);
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("An error occurred during encryption.");
            showAlert("An error occurred during encryption.", Alert.AlertType.ERROR);
            e.printStackTrace();
            return null;
        }
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.initStyle(UNDECORATED);
        alert.setHeaderText(null);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.getDialogPane().setStyle("-fx-background-color: white;" +
                "-fx-border-color: #666464; -fx-border-width: 3;" +
                "-fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment: center; -fx-padding: 10;");
        alert.setContentText(message);
        alert.showAndWait();
    }

}
