package org.event.chems;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static javafx.stage.StageStyle.UNDECORATED;

public class DateTime {

    void updateDateTime(Label setDateTimeLabel) {
        try {
            setDateTimeLabel.setText(DateTimeUpdater.getFormattedDateTime());
        } catch (Exception e) {
            System.err.println("Failed to update date and time.");
            showAlert("Failed to update date and time.", Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    public static class DateTimeUpdater {
        private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy / MM / dd");
        private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH : mm : ss");

        public static String getFormattedDateTime() {
            try {
                LocalDateTime now = LocalDateTime.now();
                String date = now.format(DATE_FORMATTER);
                String time = now.format(TIME_FORMATTER);
                return date + "\n" + time;
            } catch (DateTimeParseException e) {
                System.err.println("Error formatting date and time.");
                showAlert("Error formatting date and time.", Alert.AlertType.ERROR);
                e.printStackTrace();
                return "Invalid date/time";
            }
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
