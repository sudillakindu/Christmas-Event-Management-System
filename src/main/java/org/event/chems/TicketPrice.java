package org.event.chems;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.layout.Region;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import static javafx.stage.StageStyle.UNDECORATED;

public class TicketPrice {

    private final String appBasePath = System.getProperty("user.dir");
    private final File mainFile = new File(appBasePath, "DataFile/ticket_price.csv");
    private final File backupFile = new File(appBasePath, ".idea/Back_up/ticket_price_backup.csv");

    private final LocalDateTime currentDateTime = LocalDateTime.now();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void writeToFile(double fullTicketPrice) {
        try {
            createMainFileIfNotExists();
            createBackupFileIfNotExists();
            appendToFile(mainFile, fullTicketPrice);
            appendToFile(backupFile, fullTicketPrice);
        } catch (Exception e) {
            System.err.println("Error while saving ticket price data: " + e.getMessage());
            showAlert("Error while saving ticket price data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void createMainFileIfNotExists() {
        try {
            File mainDir = mainFile.getParentFile();
            if (!mainDir.exists() && mainDir.mkdirs()) {
                System.err.println("Main file directory created: " + mainDir);
            }
            if (!mainFile.exists() && mainFile.createNewFile()) {
                System.err.println("Main file created: " + mainFile);
            }
        } catch (IOException e) {
            System.err.println("Error creating main file: " + e.getMessage());
            showAlert("Error creating main file: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void createBackupFileIfNotExists() {
        try {
            File backupDir = backupFile.getParentFile();
            if (!backupDir.exists() && backupDir.mkdirs()) {
                System.err.println("Backup directory created: " + backupDir);
            }
            if (!backupFile.exists() && backupFile.createNewFile()) {
                System.err.println("Backup file created: " + backupFile);
            }
        } catch (IOException e) {
            System.err.println("Error creating backup file: " + e.getMessage());
            showAlert("Error creating backup file: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void appendToFile(File file, double fullTicketPrice) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(formatTicketData(fullTicketPrice));
            System.err.println("One row is successfully added to " + file.getName());
        } catch (IOException e) {
            System.err.println("Error writing to file " + file.getName() + ": " + e.getMessage());
            showAlert("Error writing to file " + file.getName() + ": " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String formatTicketData(double fullTicketPrice) {
        return String.format("%.2f,%s%n", fullTicketPrice, currentDateTime.format(formatter));
    }

    public Double readLastTicketPrice() {
        try {
            createMainFileIfNotExists();

            List<String> lines = Files.readAllLines(mainFile.toPath());

            if (lines.isEmpty()) {
                System.err.println("No ticket price found in the file. Using default price of 2000.");
                return 2000.0; // Default price
            }

            String lastLine = lines.get(lines.size() - 1);
            String[] parts = lastLine.split(",");
            return parts.length > 0 ? Double.parseDouble(parts[0]) : null;

        } catch (IOException e) {
            System.err.println("Error reading from file: " + e.getMessage());
            showAlert("Error reading from file: " + e.getMessage(), Alert.AlertType.ERROR);
            return 2000.0;
        } catch (NumberFormatException e) {
            System.err.println("Error parsing the last ticket price: " + e.getMessage());
            showAlert("Error parsing the last ticket price: " + e.getMessage(), Alert.AlertType.ERROR);
            return 2000.0;
        }
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Platform.runLater(() -> {
            Alert alert = new Alert(alertType);
            alert.initStyle(UNDECORATED);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
            alert.getDialogPane().setStyle("-fx-background-color: white;" +
                    "-fx-border-color: #666464; -fx-border-width: 3;" +
                    "-fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment: center; -fx-padding: 10;");
            alert.showAndWait();
        });
    }
}
