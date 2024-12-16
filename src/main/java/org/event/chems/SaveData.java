package org.event.chems;

import javafx.scene.control.Alert;
import javafx.scene.layout.Region;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static javafx.stage.StageStyle.UNDECORATED;

public class SaveData {

    private final File mainFile = Paths.get("DataFile", "member_data.csv").toFile();
    private final File backupFile = Paths.get(".idea", "Back_up", "member_data_backup.csv").toFile();

    private final LocalDateTime currentDateTime = LocalDateTime.now();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void writeToFile(Member member) {
        try {
            createMainFileIfNotExists();
            createBackupFileIfNotExists();
            writeDataToFile(mainFile, member);
            writeDataToFile(backupFile, member);
        } catch (Exception e) {
            System.err.println("Error while saving data: " + e.getMessage());
            showAlert("Error while saving data: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void createMainFileIfNotExists() {
        try {
            File backupDir = mainFile.getParentFile();
            if (!backupDir.exists() && backupDir.mkdirs()) {
                System.err.println("MainFile directory created : " + backupDir);
            }
            if (!mainFile.exists() && mainFile.createNewFile()) {
                System.err.println("MainFile file created : " + mainFile);
            }
        } catch (IOException e) {
            System.err.println("Error creating main file: " + e.getMessage());
            showAlert("Error creating main file: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
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
            e.printStackTrace();
        }
    }

    private void writeDataToFile(File file, Member member) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(formatMemberData(member));
            System.err.println("One row is successfully added to " + file.getName());
        } catch (IOException e) {
            System.err.println("Error writing to file " + file.getName() + ": " + e.getMessage());
            showAlert("Error writing to file " + file.getName() + ": " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private String formatMemberData(Member member) {
        return String.join(",",
                member.getChems_id(),
                member.getUser_type(),
                member.getNibm_id(),
                member.getEncryptedNibm_id(),
                member.getMember_name(),
                member.getBatch_number(),
                member.getEmail(),
                member.getPayment(),
                member.getDiscount(),
                member.getPrice(),
                currentDateTime.format(formatter),
                "No") + "\n";
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
