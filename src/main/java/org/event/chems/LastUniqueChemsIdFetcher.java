package org.event.chems;

import javafx.scene.control.Alert;
import javafx.scene.layout.Region;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static javafx.stage.StageStyle.UNDECORATED;

public class LastUniqueChemsIdFetcher {

    public static String getLastUniqueString() {
        String lastUniqueString = null;
        String nextUniqueString = null;
        String query = "SELECT chems_id FROM chemsuser ORDER BY chems_id DESC LIMIT 1";
        DatabaseServerAndDriver asd2 = new DatabaseServerAndDriver();

        try (Connection connection = DriverManager.getConnection(asd2.getDB_URL(), asd2.getDB_USER(), asd2.getDB_PASSWORD());
             PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                lastUniqueString = resultSet.getString("chems_id");
            }

            if (lastUniqueString != null) {
                nextUniqueString = LastUniqueChemsIdFetcher.generateNextString(lastUniqueString);
            } else {
                nextUniqueString = "CH150";
            }

        } catch (SQLException e) {
            System.err.println("SQL error occurred while fetching the last unique string.");
            showAlert("SQL error occurred while fetching the last unique string.", Alert.AlertType.ERROR);
            e.printStackTrace();
            return null;
        } catch (Exception e) {
            System.err.println("An unexpected error occurred.");
            showAlert("An unexpected error occurred.", Alert.AlertType.ERROR);
            e.printStackTrace();
            return null;
        }
        return nextUniqueString;
    }

    public static String generateNextString(String lastUniqueString) {
        try {
            String prefix = lastUniqueString.substring(0, 2);
            int numberPart = Integer.parseInt(lastUniqueString.substring(2));
            numberPart++;
            return prefix + String.format("%03d", numberPart);
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            System.err.println("Error generating the next unique string.");
            e.printStackTrace();
            return null;
        }
    }

    private static void showAlert(String message, Alert.AlertType alertType) {
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
