package org.event.chems;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import static javafx.stage.StageStyle.UNDECORATED;
import javafx.scene.image.Image;

public class SystemForm {

    @FXML
    private Label chemsIDLabelAuto, batchNumberLabel, paymentLabel, discountLabel, priceLabel, qrGenerator_ImageLabel, setDateTimeLabel, systemFormNameLabel;
    @FXML
    private ComboBox<String> selectedComboBoxUserType, selectedComboBoxPayment, selectedComboBoxDiscount, selectedComboBoxSearchMember, selectedComboBoxSendingEmail;
    @FXML
    private TextField NIBMIDTextField, memberNameTextField, batchNumberTextField, emailTextField, searchMember_ChemsId_NibmId_MemberName_TextField, qrGenerator_chemsIdTextField, adminPanel_ticketPriceTextField, adminPanel_SendingEmailTextField, adminPanel_SendingEmailAppPasswordTextField, adminPanel_SetCHEMSIdTextField;
    @FXML
    private Pane adminPanelPane;
    @FXML
    private TableView<MemberTable> tableView;
    @FXML
    private TableColumn<MemberTable, String> chemsIdColumn, userTypeColumn, nibmIdColumn, memberNameColumn, batchNumberColumn, emailColumn, paymentColumn, discountColumn, priceColumn;
    @FXML
    private Button generatorQrCodeButton, sendEmailButton, offeredDiscountButton, logOutButton, exitButton, countSentEmailButton, resetCountSentEmailButton;
    @FXML
    private ImageView systemFormImage, systemFormSideLogo;

    private String chems_id, user_type, nibm_id, member_name, batch_number, email, payment, discount, price;
    private double fullTicketPrice = 0.0;
    private String nextChemsId, encryptedNibm_id;

    private static Connection getConnection() throws SQLException, ClassNotFoundException {
        DatabaseServerAndDriver asd1 = new DatabaseServerAndDriver();
        Class.forName(asd1.getDB_DRIVER()); // Load the driver
        return DriverManager.getConnection(asd1.getDB_URL(), asd1.getDB_USER(), asd1.getDB_PASSWORD()); // Return connection
    }



    @FXML
    public void initialize() {
        getSystemFormImage_SideLogo();
        //systemFormName();
        setDateTime();
        startConnectionCheck();
        setupComboBoxes();
        initializeLabels(); //chemsid fullticketprice
        AutomaticallyConvert_Uppercase_LowerCase();
        setupTableColumns();
    }

    public void getSystemFormImage_SideLogo() {
        try {
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/image_SystemForm_background.png")));
            systemFormImage.setImage(image);

            Image sideLogo = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/image_title.png")));
            systemFormSideLogo.setImage(sideLogo);

        } catch (NullPointerException e) {
            //System.err.println("Error loading images: " + e.getMessage());
            showAlert("Error loading images: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void setDateTime() {
        DateTime dateTime = new DateTime();
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            if (setDateTimeLabel != null) {
                dateTime.updateDateTime(setDateTimeLabel);
            }
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
    }

    private void startConnectionCheck() {
        Thread connectionCheckThread = new Thread(new ConnectionChecker());
        connectionCheckThread.start();
    }

    private void setupComboBoxes() {
        selectedComboBoxUserType.getItems().addAll("Student", "Non-Mem", "Lecturer", "Staff", "OrgTeam");
        selectedComboBoxPayment.getItems().addAll("Paid", "Pending");
        selectedComboBoxDiscount.getItems().addAll("0%", "10%", "30%", "50%");
        selectedComboBoxSearchMember.getItems().addAll("CHEMS ID", "NIBM ID", "MEMBER NAME");

        addItems_SelectedComboBoxSendingEmail();
        setComboBoxDisableState(false);

        selectedComboBoxPayment.setValue("Paid");
        selectedComboBoxDiscount.setValue("0%");
        selectedComboBoxSearchMember.setValue("CHEMS ID");
    }

    private void setComboBoxDisableState(boolean disable) {
        selectedComboBoxSendingEmail.setDisable(disable);  // Set the ComboBox disabled or enabled based on the boolean value
    }

    private void addItems_SelectedComboBoxSendingEmail() {
        String sql = "SELECT sentemail FROM sentemailapppassword";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                String sentemail = rs.getString("sentemail");
                selectedComboBoxSendingEmail.getItems().add(sentemail);

                //System.out.println("App Password: " + sentemail); // Debugging/logging
            }

            if (!hasResults) {
                Platform.runLater(() -> showAlert("No Sent email addresses found in the database. (sentemailapppassword)", Alert.AlertType.ERROR));
            }

        } catch (SQLException | ClassNotFoundException ex) {
            Platform.runLater(() -> showAlert("Error retrieving email addresses : " + ex.getMessage(), Alert.AlertType.ERROR));
        } catch (Exception ex) {
            Platform.runLater(() -> showAlert("Error : " + ex.getMessage(), Alert.AlertType.ERROR));
        }
    }



    private void initializeLabels() {
        setFullTicketPrice();
        nextChemsId = LastUniqueChemsIdFetcher.getLastUniqueString();
        chemsIDLabelAuto.setText(nextChemsId);
        disableDiscountField(true);
        disableAdminPanel(true);
        disableGeneratorQrCodeButton(false);
        disableSendEmailButton(true); // Disable Send Email Button
    }

    //Automatically convert input to uppercase
    private void AutomaticallyConvert_Uppercase_LowerCase() {
        NIBMIDTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            NIBMIDTextField.setText(newValue.toUpperCase());
        });

        memberNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            memberNameTextField.setText(newValue.toUpperCase());
        });

        batchNumberTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            batchNumberTextField.setText(newValue.toUpperCase());
        });

        searchMember_ChemsId_NibmId_MemberName_TextField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchMember_ChemsId_NibmId_MemberName_TextField.setText(newValue.toUpperCase());
        });

        qrGenerator_chemsIdTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            qrGenerator_chemsIdTextField.setText(newValue.toUpperCase());
        });

        adminPanel_SetCHEMSIdTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            adminPanel_SetCHEMSIdTextField.setText(newValue.toUpperCase());
        });

        emailTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            emailTextField.setText(newValue.toLowerCase());
        });

        adminPanel_SendingEmailTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            adminPanel_SendingEmailTextField.setText(newValue.toLowerCase());
        });
    }

    private void setFullTicketPrice() {
        TicketPrice ticketPriceReader = new TicketPrice();
        Double lastPrice = ticketPriceReader.readLastTicketPrice();

        if (lastPrice != null) {
            System.out.println("Last ticket price: " + lastPrice);
            fullTicketPrice = lastPrice;
        } else {
            System.err.println("No ticket price found. Setting default price.");
            showAlert("No ticket price found. Setting default price.", Alert.AlertType.ERROR);
            fullTicketPrice = 0.0; // Set a default price if none is found
        }

        adminPanel_ticketPriceTextField.setText(String.format("%.0f", fullTicketPrice));
        priceLabel.setText(String.valueOf((int) fullTicketPrice));
    }

    private void disableDiscountField(boolean disable) {
        discountLabel.setDisable(disable);
        selectedComboBoxDiscount.setDisable(disable);
    }

    public void disableAdminPanel(boolean disable) {
        adminPanelPane.setDisable(disable);
    }

    private void disableSendEmailButton(boolean disable) {
        sendEmailButton.setDisable(disable);
    }

    private void disableGeneratorQrCodeButton(boolean disable) {
        generatorQrCodeButton.setDisable(disable);
    }

    private void setupTableColumns() {
        chemsIdColumn.setCellValueFactory(new PropertyValueFactory<>("chemsId"));
        userTypeColumn.setCellValueFactory(new PropertyValueFactory<>("userType"));
        nibmIdColumn.setCellValueFactory(new PropertyValueFactory<>("nibmId"));
        memberNameColumn.setCellValueFactory(new PropertyValueFactory<>("memberName"));
        batchNumberColumn.setCellValueFactory(new PropertyValueFactory<>("batchNumber"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        paymentColumn.setCellValueFactory(new PropertyValueFactory<>("payment"));
        discountColumn.setCellValueFactory(new PropertyValueFactory<>("discount"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        loadDataToTable();

        tableView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                displaySelectedRow(newValue);
            }
        });
    }

    private void loadDataToTable() {
        String query = "SELECT * FROM chemsuser ORDER BY chems_id DESC";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {

            ObservableList<MemberTable> data = FXCollections.observableArrayList();
            while (resultSet.next()) {

                //String discount = resultSet.getString("discount") != null ? resultSet.getString("discount") : "NULL";
                //String price = resultSet.getString("price") != null ? resultSet.getString("price") : "NULL";

                data.add(new MemberTable(
                        resultSet.getString("chems_id"),
                        resultSet.getString("user_type"),
                        resultSet.getString("nibm_id"),
                        resultSet.getString("encryptedNibm_id"),
                        resultSet.getString("member_name"),
                        resultSet.getString("batch_number"),
                        resultSet.getString("email"),
                        resultSet.getString("payment"),
                        resultSet.getString("discount"),
                        resultSet.getString("price")
                ));

            }
            tableView.setItems(data);
        } catch (SQLException | ClassNotFoundException e) {
            showAlert("Error loading data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void displaySelectedRow(MemberTable selectedMember) {
        try {
            if (chemsIDLabelAuto != null) {
                chemsIDLabelAuto.setText(selectedMember.getChemsId() != null ? selectedMember.getChemsId() : "NULL");
            }
            if (selectedComboBoxUserType != null) {
                selectedComboBoxUserType.setValue(selectedMember.getUserType() != null ? selectedMember.getUserType() : "NULL");
            }
            if (NIBMIDTextField != null) {
                NIBMIDTextField.setText(selectedMember.getNibmId() != null ? selectedMember.getNibmId() : "NULL");
            }
            if (memberNameTextField != null) {
                memberNameTextField.setText(selectedMember.getMemberName() != null ? selectedMember.getMemberName() : "NULL");
            }
            if (batchNumberTextField != null) {
                batchNumberTextField.setText(selectedMember.getBatchNumber() != null ? selectedMember.getBatchNumber() : "NULL");
            }
            if (emailTextField != null) {
                emailTextField.setText(selectedMember.getEmail() != null ? selectedMember.getEmail() : "NULL");
            }
            if (selectedComboBoxPayment != null) {
                selectedComboBoxPayment.setValue(selectedMember.getPayment() != null ? selectedMember.getPayment() : "NULL");
            }
            if (selectedComboBoxDiscount != null) {
                selectedComboBoxDiscount.setValue(selectedMember.getDiscount() != null ? selectedMember.getDiscount() : "NULL");
            }
            if (priceLabel != null) {
                priceLabel.setText(selectedMember.getPrice() != null ? selectedMember.getPrice() : "NULL");
            }
            if (qrGenerator_chemsIdTextField != null) {
                qrGenerator_chemsIdTextField.setText(selectedMember.getChemsId() != null ? selectedMember.getChemsId() : "NULL");
            }
            if (searchMember_ChemsId_NibmId_MemberName_TextField != null) {
                searchMember_ChemsId_NibmId_MemberName_TextField.setText(selectedMember.getChemsId() != null ? selectedMember.getChemsId() : "NULL");
                selectedComboBoxSearchMember.setValue("CHEMS ID");
            }
        } catch (Exception e) {
            System.err.println("Error displaying selected row: " + e.getMessage());
        }
    }



    @FXML
    private void selectedComboBoxUserTypeActionEvent() {
        String selectedUserType = selectedComboBoxUserType.getValue();
        if (selectedUserType == null) {
            return;
        }

        switch (selectedUserType) {
            case "Non-Mem":
                //resetDiscounts();
                setBatchNumber("OUT-NON-MEM", true);
                break;
            case "Lecturer":
                //applyFullDiscount();
                setBatchNumber("NIBM-LECTURER", true);
                break;
            case "Staff":
                //applyFullDiscount();
                setBatchNumber("NIBM-STAFF", true);
                break;
            case "OrgTeam":
                //applyFullDiscount();
                setBatchNumber("NIBM-ORG-TEAM", true);
                break;
            default:
                //resetDiscounts();
                setBatchNumber("", false);
                break;
        }

        resetDiscounts();
    }

/*
    private void applyFullDiscount() {
        if (!selectedComboBoxDiscount.getItems().contains("100%")) {
            selectedComboBoxDiscount.getItems().add("100%");
        }
        selectedComboBoxDiscount.setValue("100%");
        selectedComboBoxPayment.setValue("FREE");
        priceLabel.setText("FREE");

        disablePaymentField(true);
        disableDiscountField(true);
        discount = "100%";
        price = "FREE";
    }
*/

    private void setBatchNumber(String batchText, boolean disable) {
        if (disable) {
            batchNumberTextField.setText(batchText);
        } else {
            batchNumberTextField.clear();
        }
        batchNumberLabel.setDisable(disable);
        batchNumberTextField.setDisable(disable);
    }

    private void resetDiscounts() {
        selectedComboBoxDiscount.getItems().remove("100%");
        selectedComboBoxPayment.setValue("Paid");
        selectedComboBoxDiscount.setValue("0%");

        disablePaymentField(false);
        selectedComboBoxDiscountActionEvent();
    }

    private void disablePaymentField(boolean disable) {
        paymentLabel.setDisable(disable);
        selectedComboBoxPayment.setDisable(disable);
    }



    @FXML
    private void selectedComboBoxPaymentActionEvent() {
        String selectedPayment = selectedComboBoxPayment.getValue();
        switch (selectedPayment) {
            case "Pending" -> applyPendingPayment();
            case "FREE" -> applyFREEPayment();
            default -> resetPaymentOptions();
        }
    }

    private void applyPendingPayment() {
        if (!selectedComboBoxDiscount.getItems().contains("NULL")) {
            selectedComboBoxDiscount.getItems().add("NULL");
        }
        selectedComboBoxDiscount.setValue("NULL");
        priceLabel.setText("NULL");

        disableDiscountField(true);
        removeFreePaymentMethod();

        discount = null;
        price = null;
    }

    private void applyFREEPayment() {
        if (!selectedComboBoxDiscount.getItems().contains("100%")) {
            selectedComboBoxDiscount.getItems().add("100%");
        }
        selectedComboBoxDiscount.setValue("100%");
        priceLabel.setText("FREE");

        disableDiscountField(true);
        discount = "100%";
        price = "FREE";
    }

    private void resetPaymentOptions() {
        selectedComboBoxDiscount.getItems().removeAll("NULL");
        selectedComboBoxDiscount.setValue("0%");
        selectedComboBoxDiscountActionEvent();

        disablePaymentField(false);
        disableDiscountField(true);
        removeFreePaymentMethod();
    }

    private void removeFreePaymentMethod() {
        if (selectedComboBoxDiscount.getItems().contains("FREE")) {
            selectedComboBoxPayment.getItems().remove("FREE");
            selectedComboBoxDiscount.getItems().remove("100%");
        }

        String selectedPayment = selectedComboBoxPayment.getValue();
        if ("Paid".equals(selectedPayment)) {
            disableOfferedDiscountButton(false);
        } else if ("Pending".equals(selectedPayment)) {
            disableOfferedDiscountButton(true);
        }
    }



    @FXML
    private void selectedComboBoxDiscountActionEvent() {
        String selectedDiscount = selectedComboBoxDiscount.getValue();
        double discountPercentage = switch (selectedDiscount) {
            case "10%" -> 0.1;
            case "30%" -> 0.3;
            case "50%" -> 0.5;
            default -> 0.0;
        };

        discount = selectedDiscount;
        price = String.valueOf((int) (fullTicketPrice * (1 - discountPercentage)));
        priceLabel.setText(price);
    }



    @FXML
    private void selectedComboBoxSearchMemberActionEvent() {
        String selectedSearchMember = selectedComboBoxSearchMember.getValue();
        switch (selectedSearchMember) {
            case "CHEMS ID" -> searchMember_ChemsId_NibmId_MemberName_TextField.setPromptText("CH150");
            case "NIBM ID" -> searchMember_ChemsId_NibmId_MemberName_TextField.setPromptText("MADSE241F-001");
            case "MEMBER NAME" -> searchMember_ChemsId_NibmId_MemberName_TextField.setPromptText("SANUKA SANKALPA");
            default -> searchMember_ChemsId_NibmId_MemberName_TextField.setPromptText("Search Member");
        }
    }



    @FXML
    private void addMemberActionEvent() {
        if (!isInputValid()) {
            return;
        }

        encryptNibmId();  // Encrypt the NIBM ID based on conditions
        Member member = createMember();  // Create the member object with populated data

        if (insertMember(member)) {  // Insert into the database and handle success
            loadDataToTable();  // Reload table with the updated data
            autoGeneratorQrCode();  // Automatically generate QR code for the new member
            clearActionEvent();  // Clear the form for new input
        }
    }

    private boolean isInputValid() {
        chems_id = Optional.ofNullable(chemsIDLabelAuto.getText()).orElse("");
        user_type = selectedComboBoxUserType.getValue();
        nibm_id = Optional.ofNullable(NIBMIDTextField.getText()).orElse("");
        member_name = Optional.ofNullable(memberNameTextField.getText()).orElse("");
        batch_number = Optional.ofNullable(batchNumberTextField.getText()).orElse("");
        email = Optional.ofNullable(emailTextField.getText()).orElse("");
        payment = selectedComboBoxPayment.getValue();

        // Validate the input fields with custom error messages
        if (chems_id.isEmpty() || user_type == null || nibm_id.isEmpty() || member_name.isEmpty() || batch_number.isEmpty() || email.isEmpty()) {
            showAlert("All fields are required.", Alert.AlertType.WARNING);
            return false;
        }
        if (member_name.matches(".*\\d.*")) {
            showAlert("Member Name cannot contain numbers.", Alert.AlertType.WARNING);
            return false;
        }
        if (!isValidEmail(email)) {
            showAlert("Please enter a valid email.", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean insertMember(Member member) {
        return DatabaseConnection.insertUserData(member);
    }

    private void encryptNibmId() {
        if (/*"Lecturer".equals(user_type) || "Staff".equals(user_type) ||*/ "Paid".equals(payment) || "FREE".equals(payment)) {
            Encryptor encryptor = new Encryptor();
            encryptedNibm_id = encryptor.encryptString(encryptor.encryptString(nibm_id));
        }
    }

    private Member createMember() {
        return new Member(chems_id, user_type, nibm_id, encryptedNibm_id, member_name, batch_number, email, payment, discount, price);
    }

    private void autoGeneratorQrCode() {
        qrGenerator_chemsIdTextField.setText(chemsIDLabelAuto.getText());
        generateQrCodeForChemsId(chemsIDLabelAuto.getText());
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9][a-zA-Z0-9._%+-]*@[a-zA-Z0-9.-]+\\.(com|lk)$";
        return email.matches(emailRegex) &&
                (email.endsWith("@gmail.com") ||
                        email.endsWith("@student.nibm.lk") ||
                        email.endsWith("@nibm.lk") ||
                        email.endsWith("@vl.nibm.lk"));
    }



    @FXML
    private void clearActionEvent() {
        nextChemsId = LastUniqueChemsIdFetcher.getLastUniqueString();
        chemsIDLabelAuto.setText(nextChemsId);
        encryptedNibm_id = null;

        NIBMIDTextField.clear();
        memberNameTextField.clear();
        batchNumberTextField.clear();
        emailTextField.clear();

        selectedComboBoxUserType.setValue(null);
        selectedComboBoxPayment.setValue("Paid");
        selectedComboBoxDiscount.setValue("0%");

        priceLabel.setText(String.valueOf((int) fullTicketPrice));

        //searchMember_ChemsId_NibmId_MemberName_TextField.clear();
        //qrGenerator_chemsIdTextField.clear();
        //qrGenerator_ImageLabel.setGraphic(null);

        disableGeneratorQrCodeButton(false);
        disableSendEmailButton(true);

        disableDiscountField(true);

        adminPanel_SetCHEMSIdTextField.clear();
    }



    @FXML
    private void updateActionEvent() {
        MemberTable selectedMember = tableView.getSelectionModel().getSelectedItem();

        if (selectedMember == null) {
            showAlert("No member selected for update.", Alert.AlertType.WARNING);
            return;
        }
        if (!isInputValid()) {
            return;
        }
        encryptNibmId();

        selectedMember.setUserType(selectedComboBoxUserType.getValue());
        selectedMember.setNibmId(NIBMIDTextField.getText());
        selectedMember.setEncryptedNibmId(encryptedNibm_id);
        selectedMember.setMemberName(memberNameTextField.getText());
        selectedMember.setBatchNumber(batchNumberTextField.getText());
        selectedMember.setEmail(emailTextField.getText());
        selectedMember.setPayment(selectedComboBoxPayment.getValue());
        //selectedMember.setDiscount(selectedComboBoxDiscount.getValue());
        //selectedMember.setPrice(priceLabel.getText());

        if(Objects.equals(selectedComboBoxDiscount.getValue(), "NULL")) {
            selectedMember.setDiscount(null);
        } else {
            selectedMember.setDiscount(selectedComboBoxDiscount.getValue());
        }
        if(Objects.equals(priceLabel.getText(), "NULL")) {
            selectedMember.setPrice(null);
        } else {
            selectedMember.setPrice(priceLabel.getText());
        }

        updateMemberInDatabase(selectedMember);
        loadDataToTable();
        clearActionEvent();
    }

    private void updateMemberInDatabase(MemberTable member) {
        String query = "UPDATE chemsuser SET user_type = ?, nibm_id = ?, encryptedNibm_id = ?, member_name = ?, batch_number = ?, email = ?, payment = ?, discount = ?, price = ?, isUpdateMember = ?, updatedDT = ? WHERE chems_id = ?";

        final LocalDateTime currentDateTime = LocalDateTime.now();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, member.getUserType());
            statement.setString(2, member.getNibmId());
            statement.setString(3, member.getEncryptedNibmId());
            statement.setString(4, member.getMemberName());
            statement.setString(5, member.getBatchNumber());
            statement.setString(6, member.getEmail());
            statement.setString(7, member.getPayment());
            statement.setString(8, member.getDiscount());
            statement.setString(9, member.getPrice());
            statement.setString(10, isUpdateMember());     // The return value of isUpdateMember() is always "Yes"
            statement.setString(11, currentDateTime.format(formatter));
            statement.setString(12, member.getChemsId());

//            statement.setString(1, member.getUserType());
//            statement.setString(2, member.getEncryptedNibmId());
//            statement.setString(3, member.getMemberName());
//            statement.setString(4, member.getBatchNumber());
//            statement.setString(5, member.getEmail());
//            statement.setString(6, member.getPayment());
//            statement.setString(7, member.getDiscount());
//            statement.setString(8, member.getPrice());
//            statement.setString(9, isUpdateMember());     // The return value of isUpdateMember() is always "Yes"
//            statement.setString(10, currentDateTime.format(formatter));
//            statement.setString(11, member.getChemsId());

            int rowsUpdated = statement.executeUpdate();
            if (rowsUpdated > 0) {
                saveBackUpData(member);
                showAlert("Database Member Updated Successfully!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("No rows updated. Please check the data.", Alert.AlertType.WARNING);
            }
        } catch (SQLException | ClassNotFoundException e) {
            showAlert("Error updating data: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String isUpdateMember() {
        return "Yes";
    }

    public static void saveBackUpData(MemberTable member) {
        UpdateSaveData updateSaveData = new UpdateSaveData();
        updateSaveData.updateMemberInFile(member);
    }



    @FXML
    private void searchMemberActionEvent() {
        String selectedSearchMember = selectedComboBoxSearchMember.getValue();

        if (selectedSearchMember == null || selectedSearchMember.isBlank()) {
            // Handle case when no option is selected
            System.out.println("Please select a search criterion.");
            showAlert("Please select a search criterion.", Alert.AlertType.ERROR);
            return;
        }

        String sql = null;

        switch (selectedSearchMember) {
            case "CHEMS ID" -> sql = "SELECT * FROM chemsuser WHERE chems_id = ?";
            case "NIBM ID" -> sql = "SELECT * FROM chemsuser WHERE nibm_id = ?";
            case "MEMBER NAME" -> sql = "SELECT * FROM chemsuser WHERE member_name = ?";
            default -> {
                System.out.println("Invalid search criterion.");
                showAlert("Invalid search criterion.", Alert.AlertType.ERROR);
                return;
            }
        }

        String searchValue = searchMember_ChemsId_NibmId_MemberName_TextField.getText();

        if (searchValue == null || searchValue.isBlank()) {
            // Handle case when the search value is empty
            System.out.println("Please enter a value to search.");
            showAlert("Please enter a value to search.", Alert.AlertType.ERROR);
        } else {
            searchMember(selectedSearchMember, searchValue, sql);
        }
    }

    public void searchMember(String selectedSearchMember, String searchMemberString, String sql) {
        //String sql = "SELECT * FROM chemsuser WHERE chems_id = ?";
        ObservableList<MemberTable> data = FXCollections.observableArrayList();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, searchMemberString);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    do {
                        MemberTable member = new MemberTable(
                                rs.getString("chems_id"),
                                rs.getString("user_type"),
                                rs.getString("nibm_id"),
                                rs.getString("encryptedNibm_id"),
                                rs.getString("member_name"),
                                rs.getString("batch_number"),
                                rs.getString("email"),
                                rs.getString("payment"),
                                rs.getString("discount"),
                                rs.getString("price")
                        );
                        data.add(member);
                    } while (rs.next());
                    tableView.setItems(data);
                } else {
                    showAlert("No records found for " + selectedSearchMember + " : " + searchMemberString, Alert.AlertType.INFORMATION);
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            showAlert("Error: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }



    @FXML
    private void searchClearCHEMSIDActionEvent() {
        loadDataToTable();
        searchMember_ChemsId_NibmId_MemberName_TextField.clear();
        qrGenerator_chemsIdTextField.clear();
    }



    @FXML
    private void resetCountSentEmailActionEvent() {
        if (selectedComboBoxSendingEmail.getValue() != null && !selectedComboBoxSendingEmail.getValue().isEmpty()) {
            // Show confirmation dialog
            Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationDialog.setTitle("Confirmation");
            confirmationDialog.setHeaderText("Reset Sent Email Count");
            confirmationDialog.setContentText("Are you sure you want to reset the sent email count ?");

            Optional<ButtonType> result = confirmationDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                resetCountSentEmailFunction(selectedComboBoxSendingEmail.getValue());
            } else {
                System.out.println("Reset action canceled.");
            }
        } else {
            showAlert("Please select sent email", Alert.AlertType.ERROR);
        }
    }

    private void resetCountSentEmailFunction(String email) {
        String updateSql = "UPDATE sentemailapppassword SET countSentEmail = ? WHERE sentemail = ?";

        int resetCount = 0;

        try (Connection conn = getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(updateSql)) {

            preparedStatement.setInt(1, resetCount);
            preparedStatement.setString(2, email);

            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                showAlert("Sent email count has been reset.", Alert.AlertType.WARNING);
            } else {
                System.out.println("No matching email found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private int countSentEmail;
    @FXML
    private void countSentEmailActionEvent() {
        if (selectedComboBoxSendingEmail.getValue() != null && !selectedComboBoxSendingEmail.getValue().isEmpty()) {
            countSentEmailFunction();
            showAlert("Count Sent Email: " + countSentEmail, Alert.AlertType.WARNING);
        } else {
            showAlert("Please select sent email", Alert.AlertType.ERROR);
        }
    }

    private void countSentEmailFunction() {
        String selectedSendingEmail = selectedComboBoxSendingEmail.getValue();

        String sql = "SELECT countSentEmail FROM sentemailapppassword WHERE sentemail = ?";
        //int countSentEmail = 0;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, selectedSendingEmail);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    countSentEmail = rs.getInt("countSentEmail");
                    //System.out.println("Count Sent Email : " + countSentEmail); // Debugging/logging

                    if (countSentEmail >= 10) {
                        showAlert(countSentEmail + " emails have been sent. Please change the email", Alert.AlertType.ERROR);
                    }

                } else {
                    if (countSentEmail != 0) {
                        showAlert("No Count found for " + selectedSendingEmail, Alert.AlertType.ERROR);
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            showAlert("Error: " + ex.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception ex) {
            showAlert("Error: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void addCountSentEmailActionEvent() {
        String selectedSendingEmail = selectedComboBoxSendingEmail.getValue();  // Get the selected email

        String selectSql = "SELECT countSentEmail FROM sentemailapppassword WHERE sentemail = ?";
        String updateSql = "UPDATE sentemailapppassword SET countSentEmail = ? WHERE sentemail = ?";

        int countValueSentEmail = 0;  // Default value

        try (Connection conn = getConnection();
             // Step 1: Select the current count for the selected email
             PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {

            selectStmt.setString(1, selectedSendingEmail);

            try (ResultSet rs = selectStmt.executeQuery()) {
                if (rs.next()) {
                    // If email exists, retrieve current count
                    countValueSentEmail = rs.getInt("countSentEmail");
                    //System.err.println("Current Email Count : " + countValueSentEmail);  // Debugging/logging

                    // Step 2: Increment the count by 1
                    countValueSentEmail += 1;
                    //System.err.println("Incremented Email Count : " + countValueSentEmail);  // Debugging/logging

                    // Step 3: Update the count in the database
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, countValueSentEmail);  // Updated count
                        updateStmt.setString(2, selectedSendingEmail);  // Selected email

                        int rowsUpdated = updateStmt.executeUpdate();

                        if (rowsUpdated > 0) {
                            //System.err.println("Email count updated successfully for : " + selectedSendingEmail);
                            //showAlert("Successfully updated email count : " + countValueSentEmail, Alert.AlertType.INFORMATION);
                        } else {
                            showAlert("Failed to update email count", Alert.AlertType.ERROR);
                        }
                    }
                } else {
                    // If the email does not exist, show an alert
                    showAlert("No count found for " + selectedSendingEmail, Alert.AlertType.ERROR);
                }
            }

        } catch (SQLException | ClassNotFoundException ex) {
            showAlert("Error: " + ex.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception ex) {
            showAlert("Error: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }





    @FXML
    private void selectedComboBoxSendingEmailActionEvent() {
        String selectedSendingEmail = selectedComboBoxSendingEmail.getValue();
        //if (selectedSendingEmail != null && !selectedSendingEmail.isEmpty()) {
            sendingEmail(selectedSendingEmail);
            countSentEmailFunction();
       /* } else {
            showAlert("Please select a valid email.", Alert.AlertType.WARNING);
        }*/
    }

    public void sendingEmail(String selectedSendingEmail) {
        String sql = "SELECT apppassword FROM sentemailapppassword WHERE sentemail = ?";
        String appPassword = null;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, selectedSendingEmail);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {

                    appPassword = rs.getString("apppassword");
                    //System.out.println("App Password : " + appPassword); // Debugging/logging
                    sentEmail_AppPassword(selectedSendingEmail, appPassword);

                } else {
                    if (appPassword != null) {
                        showAlert("No App Password found for " + selectedSendingEmail, Alert.AlertType.ERROR);
                    }
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            showAlert("Error: " + ex.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception ex) {
            showAlert("Error : " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String myAccountEmail;
    private String appPassword;
    public void sentEmail_AppPassword(String myAccountEmail, String appPassword) {
        this.myAccountEmail = myAccountEmail;
        this.appPassword = appPassword;
    }
    public String getMyAccountEmail() {
        return myAccountEmail;
    }

    public String getAppPassword() {
        return appPassword;
    }



    @FXML
    private void generatorQrCodeActionEvent() {
        String chemsId = qrGenerator_chemsIdTextField.getText();
        generateQrCodeForChemsId(chemsId);
    }

    private void generateQrCodeForChemsId(String chemsId) {
        String encryptedNibmId = getDatabaseEncryptedChemsId(chemsId);

        if (encryptedNibmId == null) {
            qrGenerator_ImageLabel.setGraphic(null);
            qrGenerator_ImageLabel.setText("Payment is Pending..");
            return;
        }

        if ("Invalid_CHEMS_ID".equals(encryptedNibmId)) {
            qrGenerator_ImageLabel.setGraphic(null);
            qrGenerator_ImageLabel.setText("Invalid CHEMS ID..");
            return;
        }

        String qrCodeData = chemsId + " = " + encryptedNibmId;
        String directory = "QRCodes";
        File dir = new File(directory);

        if (!dir.exists() && !dir.mkdirs()) {
            showAlert("Failed to create QR Codes directory.", Alert.AlertType.ERROR);
            return;
        }

        String filePath = Paths.get(directory, chemsId + ".png").toString();

        try {
            generateQrCode(qrCodeData, filePath, "UTF-8", 560);
            showAlert("QR Code image created successfully and stored at : " + filePath, Alert.AlertType.INFORMATION);

            disableGeneratorQrCodeButton(true);
            disableSendEmailButton(false);
        } catch (Exception e) {
            showAlert("Error generating QR Code: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String getDatabaseEncryptedChemsId(String chemsId) {
        String encryptedNibmId = null;
        String query = "SELECT encryptedNibm_id FROM chemsuser WHERE chems_id = ?";

        try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, chemsId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    encryptedNibmId = resultSet.getString("encryptedNibm_id");
                } else {
                    showAlert("No record found for CHEMS ID : " + chemsId, Alert.AlertType.ERROR);
                    return "Invalid_CHEMS_ID";
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            showAlert("Error: " + e.getMessage(), Alert.AlertType.ERROR);
        }

        return encryptedNibmId;
    }

    private void generateQrCode(String qrCodeData, String filePath, String charset, int size) throws Exception {
        if (qrCodeData == null || qrCodeData.isEmpty()) {
            showAlert("QR code data cannot be null or empty.", Alert.AlertType.ERROR);
            return;
        }
        if (filePath == null || (!filePath.endsWith(".png") && !filePath.endsWith(".jpg"))) {
            showAlert("Invalid file format. Please use .png or .jpg.", Alert.AlertType.ERROR);
            return;
        }
        if (size <= 0) {
            showAlert("Size must be a positive integer.", Alert.AlertType.ERROR);
            return;
        }
        if (!Charset.isSupported(charset)) {
            showAlert("Unsupported charset : " + charset, Alert.AlertType.ERROR);
            return;
        }

        Map<EncodeHintType, ErrorCorrectionLevel> hintMap = new HashMap<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        BitMatrix matrix;
        try {
            matrix = new MultiFormatWriter().encode(new String(qrCodeData.getBytes(charset), charset),
                    BarcodeFormat.QR_CODE, size, size, hintMap);
        } catch (WriterException e) {
            showAlert("Error generating QR code matrix: " + e.getMessage(), Alert.AlertType.ERROR);
            return;
        }

        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(matrix);
        Path path = FileSystems.getDefault().getPath(filePath);

        try {
            MatrixToImageWriter.writeToPath(matrix, filePath.substring(filePath.lastIndexOf('.') + 1), path);
        } catch (IOException e) {
            showAlert("Error writing QR code to file: " + e.getMessage(), Alert.AlertType.ERROR);
            return;
        }

        WritableImage fxImage = null;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "png", out);
            fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
        } catch (IOException e) {
            showAlert("Error converting QR code to JavaFX Image: " + e.getMessage(), Alert.AlertType.ERROR);
            return;
        }

        ImageView qrImageView = new ImageView(fxImage);
        qrImageView.setFitWidth(200);
        qrImageView.setFitHeight(200);

        qrGenerator_ImageLabel.setText("");
        qrGenerator_ImageLabel.setGraphic(qrImageView);
    }



    @FXML
    private void sendEmailActionEvent() {
        //showAlert("Temporarily Disable This Feature", Alert.AlertType.ERROR);
        String chemsId = qrGenerator_chemsIdTextField.getText();
        MemberTable member = qrGenerator_searchMember(chemsId);

        if (member == null) {
            showAlert("No member found with CHEMS ID: " + chemsId, Alert.AlertType.INFORMATION);
            return;
        }

        String recipientEmail = member.getEmail();
        String msgContent = String.format(

                "<html><body>" +

                        "<p><img src=\"https://lh3.googleusercontent.com/fife/ALs6j_EO55P8DEtPh9BAQu3EEvVucLi6kfoyNzj32dTrYwZBP-oZF8lcrkYTXHiD67rdVYSiRoe0L51F3AV7trFQErN_g5kkjqkGC_i--2eeTFzruL-jRDiXYq82MR1LP8Eki_3YT6WRUb-KM3qq2Dq-LHqf0aKb4gxDRhzbBbm6t1Loczeclf2V_aNcoZxPLXAgSOhc7jYduvyfNMx_C1BN2bc7a7YnLmi2oEVFOh7Rq2yuHL2pq95ijgz9J92Sh12FaSp4jAsneQunY4EKWFRmTY5Ry6GrWOwV35mpgJKlivLqDyDq01ICQnh8ZVHMPD04xHeMkJNDR_sd4O7zvwlMc4dueRXu-Xz2oCTqYZENeGNmiitgYxrPagWKjJZqQj9WcZHoV2fjhe8iB0TZ1EGfrwLGyFBp3czVzbMPlmXZLXGJlEtzPLPIklM2EYhR_09IMFuS2MFVZO-gqemQy5pR9cLrli8_UDuZYZ2IXUNyCHsaIhzUw7uf-iju3wWqkt91A8zkmDXY1kiAQs28mdKUl66ucDnYVlD4aIkWH0fUBBbcQYpujIPEcpMVfvSlwE9OyxYe2juN1JXx7BDtbY7yT4be2FN2YN1affcUMN528dpL-i2hPjbUzNdXcG0NYF4LCVByu5rSHj8TxHnz-kZseii1o9QM7ju1GmQkLfC89PI4T5H2LM8oM0tUf0fpY8Jwj6Kogz6pVYEuk0zcriNUwx0o06fSZTVcMHsWdZKh_x3SOwlOB6V57-GcRQjQPs9v16vHqwHtXYXEBFLg0_PlYYAAde-mEyQ34E61ruhIpqsDUsff0hTYyol8m5N__187XDV5T-ZDzfCSWS2O5nAs13bVUzvV8jqQYKiQaMSXAuGE_1v9alEfcPN6rxS_E4IRgopThLJ8SBCWdGSNyajeyAz2YglmiKe6sLMfKbcMPSBgWRl21KXHs65SlSK--0fwVhX0XRR0JrvHGFkAA8op7zUdWyU0ZC5xGfLBmhKOmyetYCPVGIHvudqEY4xqBiP8Bnnf1w-TyooIQQjc4fFbNHZgqLZGPWSEoowZc_mPbbA1iCBfjWE7qDTQXIXowVp6qCcIdsn0-QSWx_QCwt1mAxu8KTf8a0ZjYOYLAMCuWkmMe42aqFymZMW-GaXkEVo82cr5lKX6UvBSZ1eju0UYIzISeL1ZwBQZ9zpnGGj_neNzJxiD3yq2L043H3k1udTnL5Vznjo2Z_txG-ExrlpWw_nRiGvvxNCM4tqXw53QwzBg2B1I1lwiWcvds2IGvAn_WfL1Z2CYy4-Iclx4PUdTQJNzHxPCvfOG0YsgK_zVtnZyyI-NZ5Bc3OZbZFAZzdddBUl7Ed-ynMTGQI9c3c57th-MX-j_tyu19czyp9zQxuz9hsb8pq9xrB2tGWLrKbORflZ930gNhrqNG2xCOUj0ihhSiTSKhcWkI8Vp56VJQvu5HunON054O35OFt7ACdFeCcHB3a5kPkm9qxOqeGbh8KhywVZTTDF4w-vp5nK-Z48HhdFUgbt0gYYekErcCXeu6YXxKXlaHETgP3qjlBCkLtWxHXju4rDDcixn5YEdqDyrvDjzX_V4tz48-sxJshidxhqz6ZuOuH7cS3Fuqux_xg=w1920-h911\" style=\"max-width: 100%%; border: rgb(0, 0, 0);\" width=\"auto\" height=\"auto\"></p>" +

                        "<p><strong><span style=\"font-size: 15px; font-family: Verdana, Geneva, sans-serif;\">Dear</span></strong>" +
                        "<span style=\"font-size: 15px; font-family: Verdana, Geneva, sans-serif;\">&nbsp; <strong><em><span style=\"color: rgb(184, 49, 47);\">%s</span></em></strong> ,</span></p>" +

                        "<p><span style=\"font-family: Verdana, Geneva, sans-serif; font-size: 15px;\"><strong>\uD83C\uDF89 We're delighted to have you join us for a joyous and festive Christmas celebration filled with holiday cheer! \uD83C\uDF84\n</strong></span></p>" +
                        "<p><span style=\"font-family: Verdana, Geneva, sans-serif; font-size: 15px;\">Enclosed in this email is your unique <strong>QR code ticket</strong>, granting you access to all the festivities.</span></p>" +

                        "<p><span style=\"font-size: 8px;\">&nbsp;</span></p>" +
                        "<p><span style=\"font-family: Verdana, Geneva, sans-serif; font-size: 15px;\"><strong><u>Your Details</u> :</strong></span></p>" +
                        "<table style=\"width: 100%%; border-collapse: collapse; border: solid rgb(128, 128, 128); font-size: 15px;\">" +
                        "    <tbody>" +
                        "        <tr>" +
                        "            <td style=\"width: 39.6916%%; border: solid rgb(128, 128, 128);\"><span style=\"font-family: Verdana, Geneva, sans-serif;\">&nbsp;CHEMS ID</span></td>" +
                        "            <td style=\"width: 59.1319%%; border: solid rgb(128, 128, 128);\"><span style=\"font-family: Verdana, Geneva, sans-serif; font-size: 15px;\">&nbsp;%s</span><br></span></td>" +
                        "        </tr>" +
                        "        <tr>" +
                        "            <td style=\"width: 39.6916%%; border: solid rgb(128, 128, 128);\"><span style=\"font-family: Verdana, Geneva, sans-serif;\">&nbsp;NIBM ID</span></td>" +
                        "            <td style=\"width: 59.1319%%; border: solid rgb(128, 128, 128);\"><span style=\"font-family: Verdana, Geneva, sans-serif; font-size: 15px;\">&nbsp;%s</span><br></span></td>" +
                        "        </tr>" +
                        "        <tr>" +
                        "            <td style=\"width: 39.6916%%; border: solid rgb(128, 128, 128);\"><span style=\"font-family: Verdana, Geneva, sans-serif;\">&nbsp;MEMBER NAME</span></td>" +
                        "            <td style=\"width: 59.1319%%; border: solid rgb(128, 128, 128);\"><span style=\"font-family: Verdana, Geneva, sans-serif; font-size: 15px;\">&nbsp;%s</span><br></span></td>" +
                        "        </tr>" +
                        "        <tr>" +
                        "            <td style=\"width: 39.6916%%; border: solid rgb(128, 128, 128);\"><span style=\"font-family: Verdana, Geneva, sans-serif;\">&nbsp;BATCH</span></td>" +
                        "            <td style=\"width: 59.1319%%; border: solid rgb(128, 128, 128);\"><span style=\"font-family: Verdana, Geneva, sans-serif; font-size: 15px;\">&nbsp;%s</span><br></span></td>" +
                        "        </tr>" +
                        "        <tr>" +
                        "            <td style=\"width: 39.6916%%; border: solid rgb(128, 128, 128);\"><span style=\"font-family: Verdana, Geneva, sans-serif;\">&nbsp;TICKET PRICE</span></td>" +
                        "            <td style=\"width: 59.1319%%; border: solid rgb(128, 128, 128);\"><span style=\"font-family: Verdana, Geneva, sans-serif;\">&nbsp;<span style=\"font-size: 15px;\">Rs.%s</span></span></td>" +
                        "        </tr>" +
                        "        <tr>" +
                        "            <td style=\"width: 99.4118%%; border: solid rgb(255, 0, 0);\" colspan=\"2\">" +
                        "                <div style=\"text-align: justify; line-height: 1.5;\"><span style=\"font-family: Verdana, Geneva, sans-serif;\"><span style=\"font-size: 13px;\">&nbsp;TICKET CODE&nbsp;</span><strong>:</strong><span style=\"font-family: Verdana, Geneva, sans-serif;\"><span style=\"font-size: 13px;\">&nbsp;%s</span></span></div>" +
                        "            </td>" +
                        "        </tr>" +
                        "    </tbody>" +
                        "</table>" +

                        "<p><span style=\"font-size: 8px;\">&nbsp;</span></p>" +
                        "<p><span style=\"font-family: Verdana, Geneva, sans-serif; font-size: 15px;\"><strong><u>Event Details</u> :</strong></span></p>" +
                        "<p><span style=\"font-family: Verdana, Geneva, sans-serif; font-size: 15px;\">📅 Date - <a href=\"https://linktr.ee/winterblizz24\">December 9, 2024</a></span></p>" +
                        "<p><span style=\"font-family: Verdana, Geneva, sans-serif; font-size: 15px;\">⏰ Time - 9:30 AM to 5:30 PM</span></p>" +
                        "<p><span style=\"font-family: Verdana, Geneva, sans-serif; font-size: 15px;\">📍 Location - <a href=\"https://linktr.ee/winterblizz24\">Pearl Palace Hotel</a></span></p>" +
                        "<p><span style=\"font-family: Verdana, Geneva, sans-serif; font-size: 15px;\">🎨 Dress Code - Red & Black</span></p>" +

                        "<p><span style=\"font-size: 8px;\">&nbsp;</span></p>" +
                        "<p><span style=\"font-family: Verdana, Geneva, sans-serif; font-size: 15px;\"><strong><u>Contact Info</u> :</strong></span></p>" +
                        "<p><span style=\"font-family: Verdana, Geneva, sans-serif; font-size: 15px;\">✨ We look forward to celebrating this joyous occasion with you! ✨</span></p>" +
                        "<p><span style=\"font-family: Verdana, Geneva, sans-serif; font-size: 15px;\">If you have any questions, feel free to contact us at:</span></p>" +
                        "<p><span style=\"font-family: Verdana, Geneva, sans-serif; font-size: 15px;\">📨 Email - winterblizz.24@gmail.com</span></p>" +
                        "<p><span style=\"font-family: Verdana, Geneva, sans-serif; font-size: 15px;\">📞 Sudil Lakindu - 0723356907</span></p>" +
                        "<p><span style=\"font-family: Verdana, Geneva, sans-serif; font-size: 15px;\">📞 Praveen Sankalpa - 0764907059</span></p>" +

                        "<p><span style=\"font-size: 8px;\">&nbsp;</span></p>" +
                        "<p><span style=\"font-size: 15px;font-family: Verdana, Geneva, sans-serif;\"><strong><u>Please Note</u> :</strong></span></p>" +
                        "<p><span style=\"color: rgb(184, 49, 47);font-size: 15px;font-family: Verdana, Geneva, sans-serif;\">🎫 Your QR code is unique to you and is strictly non-transferable.</span></p>" +
                        "<p><span style=\"color: rgb(184, 49, 47);font-size: 15px;font-family: Verdana, Geneva, sans-serif;\">For security reasons, do not share this code with others, as each ticket can only be scanned once at entry.</span></p>" +

                        "<p><img src=\"https://lh3.googleusercontent.com/fife/ALs6j_HlnSRFLXe5m3mvYBi1XG4guCIyAuqiaNcBBqCfnROQqG3Y6ko5UmdxDD4UYd9NOgZ6sd_rm5Df7m35s7hvQMfbVdO-WdMAJq_2xC5GEzFPpGdsuKGh0Y7Z0lEANGQ3CArtyCqKQEdi3w_2Xqd79bgMOWOJQf3rjF9-31cB5_S3JrXZv9bxLulON4i3lfj_A9nDCzIx8F_A2-yGAsqti6dlEBdzpclGgeym1Xfc7BTFp_1NgkaHNc01sU7CwbFUi6OBAYFsL_NNefmvGddhBT6fqdOu3QVMYGdGAiscOhdG1q0QrOU_pJQVFF98ndBluMzOk9FW2pY8AaUZIppY-9hMN-lYEUsv3Nv07c3yiuw1Ie4E-7LU-Y07aMC0YXOynr3lWpsUkzQBf6Kalh4NUq7hXXUDCLJ_vtxdM3MOlshf-0Pw729-3OH0o99hq3tRMvxloVTuQ1M0u6hGiNbp9JZ1nIv_dcDeF76CmZ1Kf_tJs_7fg2jhchbuoXmGidCwZCWl7WEnrVgdlowKMtaVEngv3SCyR3dlTv-m82oR5ph3aZvTb7uMU4bONzrC4-G51i13hLM3mJj_llUrvz78NT_6po3QxpN3dtAmI-9TOggM_hx3MWctE3Iru1PLCqQMfwe7iD97QYCqGaUXZTro74idWjGkT_UAgz9lO8Cp3SQ4u5xG0RMTcGduZqHCw8AelWVz5ax0mYzico5kfokgKrWSzHcV2GWJ05qBXayqjc9EE57wMXOTvfVa04iB8CIvIarTbPFjk3OhW1oOkVihbywFokImkXqJpyLlfjgswm4ZBNwoARCG9E_M37rEDhyRUCTqNuIavmzRRRB_wzhlG9JibvmknUmxuj7a6wkMN7D9KlZcITN3W9IjEuZpSlLfVRje3_jelv9g2byZhM3huQZ4vZRrMvQsUKzd5DpCy4R3wa4Kn_2PdlUvspP8zrmo7CZFRKd2jGPgg_lY_krpeVwhds1aBPwUO8yg7RjdXjCmVX-1gn-TOFyfv1JehvXOaXarJDM4fE8USJA9DCmzmfnqQ6-J5XnOrRq6vx031C42dxdQ0NICxclWunif4nhkBfqlpYH9pKKmcUBDgpCHgOnyFQac4vITkjcDW9rAnHxgJL-KMrEG2EHJ3nzmQzYQZM2ju1IcJgKkOSa-66FqnHeRoXYzcVJgZ8kkE-PSXWawlRbBZhfXdNN1AKYZWLv9xiG_hIXDHJCF_--Z9K4m099LrkBS3-UxVqaIV_b0iRd7hbeGf1pvzeUk8z44URWACopg9rHcaO9a_V-rPk4GghhfXx2U-MENRPShkjByKjCHyFUuGSIvlcVDLCkOZap1FfKGEc1XdP2ElvTAtaJqxUDPDC5sh5nYYYLPY2oNMLb_FQusaiRkGAqcdKzwzVfv4xULJM9eqpjPip9bQVmpzHDYrlHIaJ2d9bU3coXPvQjAOxmKaOX0Najzofb4SkEPXbx7buvsWhcRRR4v0w0svAF8fbIJibgnhjFRhWQFJFRXXpleEbBGWWPuHzZYUKQaRg3uGKo1U-1tv76dI3_KnudA-z01Ov_AczY2R6nPHEYc-ConV-vZ38Tlv8WOAt3QsoSUKoBOB42YD4OJABrLkg=w1920-h910\" style=\"max-width: auto; border: rgb(0, 0, 0);\" width=\"auto\" height=\"auto\"></p>" +
                        //"<p><span style=\"font-size: 11px;\"><img src=\"https://myfiles.space/user_files/244390_dfafa5cf280a8853/244390_custom_files/img1733167439.png\" width=\"auto\" height=\"auto\">&nbsp;</span></p>" +

                        "<p><span style=\"font-size: 8px;\">&nbsp;</span></p>" +
                        "<p><span style=\"font-family: Verdana, Geneva, sans-serif; font-size: 15px;\"><strong><u>Warm regards</u> ,</strong></span></p>" +
                        "<p><span style=\"color: rgb(184, 49, 47); font-family: Verdana, Geneva, sans-serif;\"><strong><span style=\"font-size: 10px;\"><em>WinterBlizz'24</em></span></strong></span></p>" +
                        "<p><span style=\"color: rgb(184, 49, 47); font-family: Verdana, Geneva, sans-serif;\"><strong><span style=\"font-size: 10px;\"><em>විසිහතරයි දශම එකේ 'අපි - MADSE24.1F</em></span></strong></span></p>" +
                        "<p><span style=\"color: rgb(184, 49, 47); font-family: Verdana, Geneva, sans-serif;\"><strong><span style=\"font-size: 10px;\"><em>National Institute of Business Management</em></span></strong></span></p>" +

                        //"<p><img src=\"https://myfiles.space/user_files/244390_dfafa5cf280a8853/244390_custom_files/img1730401189.png\" style=\"float: left; max-width: 100%%; border: solid rgb(128, 128, 128); width: 113px; height: 113px;\" width=\"113\" height=\"113\"></p>" +
                        //"<p><img src=\"https://lh3.googleusercontent.com/fife/ALs6j_G6CI8HdeKFtGMJbPcSIP860ypmquJDJttzzKhRfSN7ocQeAne5s1zJr5jxSPik9jFBaeIavI6Odrk8ojPS01szjhJ4c7qm9KI50DFykF7jT0mRcsSCru745v0zh1vAOIPTPieQ4AYxAEoA4w0DKHkRB99yoc3ggJd7CmBflOmgp8f2mUb1x4A0k_aKzAE98ewEX2ld4ka-UouqN_7lgYVb_OG5tpO4bloslt7vS2VEjuhVmFNR5FqUqmG5flgwZJhJxEYU10sXb3cqo6HUwhu4Fb6Fo0ujUDoblfAcmjSG0HoB8r9IiXywyMxkzryzz5Vh9frxxSQdRm0zIoxVDuPCZRLplYDegF1etmf9B1G9MN_egKqyiwru1ylZiL24JY_4T6K3AVJ5UZmRi1hfsdtsYmrPmU0it7YyJQAMcB9bnUq6eARnHp35L0jrdznnwp5OGRjy7P9xs7vyZdbiCccMlURzfTw5H3XcoWavMHdqRD4MjW5zT3bxW7odxErXHxnTpwMWSCXeEx_2Or5G5cx6BPeWGV0g309hLGBy6-Y5FKVgjMjiFtSNphD_OneEqlGTBSOmPF_NZX9CtdoGGE92CnC0dvOibdFJk28xRNYqMUhiC4nkuKhWutovo1evfQKWfG45K-YGUIS7O4H5ju8BrKI8qSUSJUASGj6s4UWNllKOUBYPBe_SPGnO5zmKE4yroRySfvFIRN3FkQd6DuwT1w5RpWPgB_WW5aVDfiJkxp-84Ed7j0ZtnwmZfZTVrMerslepPvJOve4B0bY1gY94cKiYCVQ99FifVORwX6WlnbIrk0nDeD3_51UiOWj848nsr7HTC7vtCJj06AMdiL1S1MnOPODkS2XFsMzAQDUzY-U8W-FOkw2C_8mrpJ3rVOvHEGKIvJvbp6AkrTwt5PGBWT_QgGzMEIHARrg0ejWk1I-mXY_Vbri0LoyqZONa0RTONy-yyQXfllUU_AJvYIIAj_8zD0ylOpxAYRu3Ylz8yVBtlYFYMIli_O-OvypGBbOXRiOQpB1ilR0MbfUI-UU8wEl7RxGyGWvNgDxSjA1o7p1CoAkkTkDSw7K2nOmwY2a3nOJIuho0XmcexSGG-kpEB6g8StFmGTm_q1iPdgG0RKhnKo6hKO0b_xE7PrCM4uROng5A932ADYDGPGtlD5M4_fDPy4W4NtxMo8GL_euJI8ZStI1FUY4WsO5l3Zxktz4IW_VfhhYh7BukQNMpnAj-t-akznQ-cnykFIHzJVdI3wYqVEInvWP5Xw-bpjdnpSjY13rglU90QcT11Wm5A734_qgNAb8L4G-4DzVwyk_dMhyTTlxcOMFS93Onvf0HvrnDcCYxlrlTr4v3cm98Go7V-rdJPtwpJfJE9HCKi-vCY1fgBBLL2DNCKE_y2q79hm68kPRbTrGLvBZOuBnvEp2Xo11iiNO9DvcoFViGAWV5gjB0IpJJM-j1Itu1g9pGULqexhOBJ8dUExmMoEx_DsGIMVpH_NKq150NyOMifPMv-ynHUA5_R6SRAJbGDrGp-16qp8V0Bybw2xoj4oRbXRtzAo1QC4_Ymrlr8G2Cw16ztLyxunAvOSLchyeZ9S_Ng4RRC8uhUe9L85ddirxrTg=w3000-h6501\" style=\"float: left; max-width: 100%%; border: solid rgb(128, 128, 128); width: 113px; height: 113px;\" width=\"113\" height=\"113\"></p>" +

                        "</body></html>",

                member.getMemberName(),    // Replace with actual member name
                member.getChemsId(),       // Replace with actual CHEMS ID
                member.getNibmId(),        // Replace with actual NIBM ID
                member.getMemberName(),    // Replace with actual member name
                member.getBatchNumber(),   // Replace with actual batch number
                member.getPrice(),       // Replace with actual ticket price
                member.getEncryptedNibmId()     // Replace with actual ticket code

        );

        String qrCodeFilePath = "QRCodes/" + chemsId + ".png";

        if (selectedComboBoxSendingEmail.getValue() != null) {
            try {
                String myEmail = getMyAccountEmail();
                String appPassword = getAppPassword();

                if (myEmail != null && appPassword != null) {
                    //System.err.println(myEmail);
                    //System.err.println(appPassword);

                    setComboBoxDisableState(true);

                    SendEmail sendEmail = new SendEmail();
                    sendEmail.sendEmail(recipientEmail, msgContent, qrCodeFilePath, myEmail, appPassword);

                    if (sendEmail.isEmailSentSuccessfully()) {
                        addCountSentEmailActionEvent();
                    }

                } else {
                    showAlert("Invalid email credentials", Alert.AlertType.ERROR);
                }
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("Failed to send email : " + e.getMessage(), Alert.AlertType.ERROR);
            }
        } else {
            showAlert("Please select Sent email", Alert.AlertType.ERROR);
        }

        setComboBoxDisableState(false);
        qr_clearCHEMSIDActionEvent();
    }

    public MemberTable qrGenerator_searchMember(String chemsId) {
        String sql = "SELECT * FROM chemsuser WHERE chems_id = ?";
        MemberTable member = null;

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, chemsId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    member = new MemberTable(
                            rs.getString("chems_id"),
                            rs.getString("user_type"),
                            rs.getString("nibm_id"),
                            rs.getString("encryptedNibm_id"),
                            rs.getString("member_name"),
                            rs.getString("batch_number"),
                            rs.getString("email"),
                            rs.getString("payment"),
                            rs.getString("discount"),
                            rs.getString("price")
                    );
                } else {
                    showAlert("No records found for CHEMS ID: " + chemsId, Alert.AlertType.INFORMATION);
                }
            }
        } catch (SQLException | ClassNotFoundException ex) {
            showAlert("Database error: " + ex.getMessage(), Alert.AlertType.ERROR);
        }

        return member;
    }



    @FXML
    private void qr_clearCHEMSIDActionEvent() {
        qrGenerator_ImageLabel.setGraphic(null);
        qrGenerator_chemsIdTextField.clear();
        searchMember_ChemsId_NibmId_MemberName_TextField.clear();

        disableGeneratorQrCodeButton(false);
        disableSendEmailButton(true);
    }



    @FXML
    private void deleteMemberActionEvent() {
        MemberTable selectedMember = tableView.getSelectionModel().getSelectedItem();

        if (selectedMember != null) {
            deleteMemberFromDatabase(selectedMember.getChemsId());
            tableView.getItems().remove(selectedMember); // Updates the TableView
        } else {
            showAlert("Please select a member to delete.", Alert.AlertType.WARNING);
        }
    }

    private void deleteMemberFromDatabase(String memberId) {
        String deleteMemberQuery = "DELETE FROM chemsuser WHERE chems_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement deleteMemberStatement = connection.prepareStatement(deleteMemberQuery)) {

            deleteMemberStatement.setString(1, memberId);
            int rowsAffected = deleteMemberStatement.executeUpdate();

            if (rowsAffected > 0) {
                showAlert("Successfully deleted member with chems_id: " + memberId, Alert.AlertType.INFORMATION);
            } else {
                showAlert("No member found with chems_id: " + memberId, Alert.AlertType.WARNING);
            }

        } catch (SQLException | ClassNotFoundException e) {
            showAlert("Error deleting member: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }



    @FXML
    private void setCHEMSIdActionEvent() {
        String inputText = adminPanel_SetCHEMSIdTextField.getText();

        if (inputText == null || inputText.trim().isEmpty()) {
            showAlert("Please enter a valid CHEMS ID.", Alert.AlertType.ERROR);

        } else if (!inputText.matches("CH\\d{3}")) { // Validate with regex: "CH" followed by exactly 3 digits
            showAlert("Invalid CHEMS ID format. Please enter in the format 'CH###'.", Alert.AlertType.ERROR);

        } else {
            chemsIDLabelAuto.setText(inputText);
            // adminPanel_SetCHEMSIdTextField.clear();
        }
    }




    @FXML
    private void freePaymentActionEvent() {
        addFreePaymentMethod();
    }

    private void addFreePaymentMethod() {
        if (!selectedComboBoxPayment.getItems().contains("FREE")) {
            selectedComboBoxPayment.getItems().add("FREE");
        }
        selectedComboBoxPayment.setValue("FREE");
        selectedComboBoxDiscount.setValue("100%");
        priceLabel.setText("FREE");

        disableOfferedDiscountButton(true);
        disableDiscountField(true);

        discount = "100%";
        price = "FREE";
    }

    private void disableOfferedDiscountButton(boolean disable) {
        offeredDiscountButton.setDisable(disable);
    }



    @FXML
    private void availableDiscountActionEvent() {
        disableDiscountField(false);
    }



    @FXML
    private void sendingSEmailActionEvent() {
        String query = "INSERT INTO sentemailapppassword (sentemail, apppassword) VALUES (?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            String email = adminPanel_SendingEmailTextField.getText();
            String appPassword = adminPanel_SendingEmailAppPasswordTextField.getText();

            if (!isSEmailValid(email)) {
                showAlert("Invalid email address. Please enter a valid one.", Alert.AlertType.ERROR);
                return;
            }

            if (!isPasswordSecure(appPassword)) {
                showAlert("Password is not secure enough. Please use a stronger password.", Alert.AlertType.ERROR);
                return;
            }

            preparedStatement.setString(1, email);
            preparedStatement.setString(2, encryptPassword(appPassword)); // Encrypt before storing.

            if (preparedStatement.executeUpdate() > 0) {
                showAlert("Sending Email & Email App Password is successfully added", Alert.AlertType.INFORMATION);

                // Clear the ComboBox items and refresh it with updated values
                selectedComboBoxSendingEmail.getItems().clear();
                addItems_SelectedComboBoxSendingEmail();

                adminPanel_SendingEmailTextField.clear();
                adminPanel_SendingEmailAppPasswordTextField.clear();
            }
        } catch (SQLException | ClassNotFoundException e) {
            System.err.println("Error inserting data: " + e.getMessage());
            DatabaseConnection.PopUpMessage.showErrorDatabaseAdded("Error inserting data: " + e.getMessage());
        }
    }

    // Helper Methods
    private boolean isSEmailValid(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@gmail\\.com$");
    }

    private boolean isPasswordSecure(String password) {
        // Regex: four groups of four alphabetic characters separated by spaces
        return password != null && password.matches("^[a-zA-Z]{4} [a-zA-Z]{4} [a-zA-Z]{4} [a-zA-Z]{4}$");
    }


    private String encryptPassword(String password) {
        // Placeholder for encryption logic.
        return password; // Replace with actual encryption.
    }




    @FXML
    private void sendingSEmailDeletActionEvent() {
            String query = "DELETE FROM sentemailapppassword WHERE sentemail = ?";

            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {

                String email = adminPanel_SendingEmailTextField.getText();

                if (!isSEmailValid(email)) {
                    showAlert("Invalid email address. Please enter a valid one.", Alert.AlertType.ERROR);
                    return;
                }

                preparedStatement.setString(1, email);

                int rowsAffected = preparedStatement.executeUpdate();
                if (rowsAffected > 0) {

                    showAlert("Email and App Password successfully deleted.", Alert.AlertType.INFORMATION);

                    // Clear the ComboBox items and refresh it with updated values
                    selectedComboBoxSendingEmail.getItems().clear();
                    addItems_SelectedComboBoxSendingEmail();

                    adminPanel_SendingEmailTextField.clear();
                    adminPanel_SendingEmailAppPasswordTextField.clear();

                } else {
                    showAlert("No entry found for the provided email.", Alert.AlertType.WARNING);
                }

            } catch (SQLException | ClassNotFoundException e) {
                System.err.println("Error deleting data: " + e.getMessage());
                DatabaseConnection.PopUpMessage.showErrorDatabaseAdded("Error deleting data: " + e.getMessage());
            }
    }



    @FXML
    private void setPriceActionEvent() {
        try {
            double price = Double.parseDouble(adminPanel_ticketPriceTextField.getText());

            TicketPrice ticketPrice = new TicketPrice();
            ticketPrice.writeToFile(price);

            setFullTicketPrice();
            priceLabel.setText(String.valueOf((int) fullTicketPrice));

            showAlert(String.format("Set New Ticket Price: Rs.%.0f /=", fullTicketPrice), Alert.AlertType.INFORMATION);

        } catch (NumberFormatException e) {
            showAlert("Invalid input! Please enter a valid number for the ticket price.", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("An unexpected error occurred: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }



    @FXML
    private void viewChartActionEvent() {
        new ChartAnalysisPreview();
    }



    @FXML
    private void logOutAdminActionEvent() {
        disableAdminPanel(true);
    }



    @FXML
    private void logOutActionEvent() {
        try {
            Stage primaryStage = (Stage) logOutButton.getScene().getWindow();
            Parent loginForm = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("LoginForm.fxml")));

            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), primaryStage.getScene().getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(event -> {
                Scene loginScene = new Scene(loginForm);
                primaryStage.setScene(loginScene);
                primaryStage.setTitle("Chems Login");
                startFadeInTransition(loginForm);
            });
            fadeOut.play();
        } catch (IOException e) {
            showAlert("Error loading login screen: " + e.getMessage(), Alert.AlertType.ERROR);
        }

    }

    private void startFadeInTransition(Parent root) {
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), root);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }



    @FXML
    private void adminActionEvent() {
        AdminAccess adminAction = new AdminAccess(this);
        //adminAction.AdminAccessSystemForm(this);
        adminAction.setVisible(true);
    }



    @FXML
    private void exitApplication() {
            Platform.exit();
            System.exit(0);
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