package org.event.chems;

import javafx.scene.control.Alert;
import javafx.scene.layout.Region;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import static javafx.stage.StageStyle.UNDECORATED;

public class UpdateSaveData {

    private final LocalDateTime currentDateTime = LocalDateTime.now();
    private final Path mainFilePath = Paths.get("DataFile", "member_data.csv");
    private final Path backupFilePath = Paths.get(".idea", "Back_up", "member_data_backup.csv");
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void updateMemberInFile(MemberTable updatedMember) {
        boolean isUpdatedMainFile = updateFile(mainFilePath, updatedMember);
        boolean isUpdatedBackupFile = updateFile(backupFilePath, updatedMember);
        if (!isUpdatedMainFile) {
            System.err.println("Member with ID " + updatedMember.getChemsId() + " not found in main file.");
            showAlert("Member with ID " + updatedMember.getChemsId() + " not found in main file.", Alert.AlertType.ERROR);
        }
        if (!isUpdatedBackupFile) {
            System.err.println("Member with ID " + updatedMember.getChemsId() + " not found in backup file.");
            showAlert("Member with ID " + updatedMember.getChemsId() + " not found in backup file.", Alert.AlertType.ERROR);
        }
    }

    private boolean updateFile(Path filePath, MemberTable updatedMember) {
        List<String> updatedRecords = new ArrayList<>();
        boolean isUpdated = false;
        String previousTimestamp = null;

        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(updatedMember.getChemsId() + ",")) {
                    // Extract the previous timestamp from the current line
                    String[] fields = line.split(",");
                    if (fields.length > 10) {
                        previousTimestamp = fields[10];
                    }
                    updatedRecords.add(formatMemberData(updatedMember, previousTimestamp));
                    isUpdated = true;
                } else {
                    updatedRecords.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading from file: " + filePath.getFileName() + " - " + e.getMessage());
            showAlert("Error reading from file: " + filePath.getFileName() + " - " + e.getMessage(), Alert.AlertType.ERROR);
            return false;
        }
        if (isUpdated) {
            try {
                writeToFileSafely(filePath, updatedRecords);
                return true;
            } catch (IOException e) {
                System.err.println("Error updating file: " + filePath.getFileName() + " - " + e.getMessage());
                showAlert("Error updating file: " + filePath.getFileName() + " - " + e.getMessage(), Alert.AlertType.ERROR);
                return false;
            }
        }
        return false;
    }

    private void writeToFileSafely(Path filePath, List<String> records) throws IOException {
        Path parentDirectory = filePath.getParent();
        if (parentDirectory != null) {
            if (!Files.exists(parentDirectory)) {
                Files.createDirectories(parentDirectory);
            }
        } else {
            parentDirectory = Paths.get(System.getProperty("java.io.tmpdir"));
        }
        Path tempFilePath = Files.createTempFile(parentDirectory, "temp", ".csv");
        try (BufferedWriter writer = Files.newBufferedWriter(tempFilePath)) {
            for (String record : records) {
                writer.write(record);
                writer.newLine();
            }
        }
        Files.move(tempFilePath, filePath, StandardCopyOption.REPLACE_EXISTING);
        System.err.println("File " + filePath.getFileName() + " updated successfully.");
    }

    private String formatMemberData(MemberTable member, String previousTimestamp) {
        return String.join(",",
                member.getChemsId(),
                member.getUserType(),
                member.getNibmId(),
                member.getEncryptedNibmId(),
                member.getMemberName(),
                member.getBatchNumber(),
                member.getEmail(),
                member.getPayment(),
                member.getDiscount(),
                member.getPrice(),
                previousTimestamp != null ? previousTimestamp : "",  // Use the previous timestamp
                "Yes",
                currentDateTime.format(formatter));
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