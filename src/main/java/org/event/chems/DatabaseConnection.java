package org.event.chems;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.layout.Region;
import java.sql.*;

import static javafx.stage.StageStyle.UNDECORATED;

class ConnectionChecker implements Runnable {
    private boolean wasConnected = true;

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                boolean isConnected = DatabaseConnection.checkDatabaseConnection();
                if (wasConnected && !isConnected) {
                    Platform.runLater(() -> DatabaseConnection.PopUpMessage.showErrorDatabaseAdded("Database connection lost!"));
                    System.out.println("Database is DOWN or unreachable.");
                } else if (!wasConnected && isConnected) {
                    Platform.runLater(() -> DatabaseConnection.PopUpMessage.showSuccessDatabaseAdded("Database connection restored!"));
                    System.out.println("Database connection restored.");
                }
                wasConnected = isConnected;
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Connection checker interrupted: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error : " + e.getMessage());
            }
        }
    }
}

public class DatabaseConnection {

    public static boolean checkDatabaseConnection() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Database connection unsuccessful: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Error : " + e.getMessage());
            return false;
        }
    }

    public static boolean insertUserData(Member member) {
        if (!isMemberDataValid(member)) {
            return false;
        }

        String query = "INSERT INTO chemsuser (chems_id, user_type, nibm_id, encryptedNibm_id, member_name, batch_number, email, payment, discount, price, isUpdateMember) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, member.getChems_id());
            preparedStatement.setString(2, member.getUser_type());
            preparedStatement.setString(3, member.getNibm_id());
            preparedStatement.setString(4, member.getEncryptedNibm_id());
            preparedStatement.setString(5, member.getMember_name());
            preparedStatement.setString(6, member.getBatch_number());
            preparedStatement.setString(7, member.getEmail());
            preparedStatement.setString(8, member.getPayment());
            preparedStatement.setString(9, member.getDiscount());
            preparedStatement.setString(10, member.getPrice());
            preparedStatement.setString(11, isUpdateMember());

            if (preparedStatement.executeUpdate() > 0) {
                logMemberInfo(member);
                saveBackUpData(member);
                PopUpMessage.showSuccessDatabaseAdded("One row is successfully added.");
                return true;
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error inserting data: " + e.getMessage());
            PopUpMessage.showErrorDatabaseAdded("Error inserting data: " + e.getMessage());
        }
        return false;
    }

    private static String isUpdateMember() {
        return "No";
    }

    private static void logMemberInfo(Member member) {
        System.out.printf("CHEMS ID: %s%nUser Type: %s%nNIBM ID: %s || Encrypted NIBM ID: %s%nMember Name: %s%nBatch Number: %s%nEmail: %s%nPayment: %s%nDiscount: %s%nPrice: %s%n",
                member.getChems_id(), member.getUser_type(), member.getNibm_id(), member.getEncryptedNibm_id(),
                member.getMember_name(), member.getBatch_number(), member.getEmail(), member.getPayment(),
                member.getDiscount(), member.getPrice());
    }

    private static boolean isMemberDataValid(Member member) {
        return member.getChems_id() != null &&
                member.getUser_type() != null &&
                member.getNibm_id() != null &&
                member.getMember_name() != null &&
                member.getBatch_number() != null &&
                member.getEmail() != null &&
                member.getPayment() != null;
    }

    private static Connection getConnection() throws SQLException, ClassNotFoundException {
        DatabaseServerAndDriver dbConfig = new DatabaseServerAndDriver();
        Class.forName(dbConfig.getDB_DRIVER());
        return DriverManager.getConnection(dbConfig.getDB_URL(), dbConfig.getDB_USER(), dbConfig.getDB_PASSWORD());
    }

    public static void saveBackUpData(Member member) {
        new SaveData().writeToFile(member);
    }

    public static class PopUpMessage {
        public static void showSuccessDatabaseAdded(String message) {
            showAlert(Alert.AlertType.INFORMATION, "Success", message, "green");
        }

        public static void showErrorDatabaseAdded(String message) {
            showAlert(Alert.AlertType.ERROR, "Error", message, "red");
        }

        private static void showAlert(Alert.AlertType alertType, String title, String message, String color) {
            Alert alert = new Alert(alertType);
            alert.initStyle(UNDECORATED);
            alert.setHeaderText(null);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
            alert.getDialogPane().setStyle("-fx-background-color: " + color + "; " +
                    "-fx-border-color: #666464; -fx-border-width: 3; " +
                    "-fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-font-weight: bold;");
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
}
