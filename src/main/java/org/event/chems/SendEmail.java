package org.event.chems;

import javafx.scene.control.Alert;
import javafx.scene.layout.Region;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javafx.stage.StageStyle.UNDECORATED;

public class SendEmail {

    public boolean isEmailSentSuccessfully() {
        return emailSentSuccessfully;
    }

    private boolean emailSentSuccessfully;

    public void sendEmail(String recipientEmail, String msgContent, String qrCodeFilePath, String myAccountEmail, String password) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");

        //String myAccountEmail = "winterblizz.24@gmail.com";
        //String password = "kpaj apcv frgp zqei";

        String ccEmail = "madse241f.nibm@gmail.com";
        String bccEmail = "masudillakindu@gmail.com";
        String msgSubject = "Celebrate Christmas with Us – Ticket Details Included! 🎫";

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myAccountEmail, password);
            }
        });

        try {
            emailSentSuccessfully = false;

            Message message = prepareMessage(session, myAccountEmail, recipientEmail, ccEmail, bccEmail, msgSubject, msgContent, qrCodeFilePath);
            if (message != null) {
                Transport.send(message);
                showAlert("Email sent successfully!\nEmail: " + recipientEmail, Alert.AlertType.INFORMATION, "green");
                emailSentSuccessfully = true;
            } else {
                showAlert("Error preparing email. Please try again.", Alert.AlertType.ERROR, "red");
            }
        } catch (MessagingException e) {
            //Logger.getLogger(SendEmail.class.getName()).log(Level.SEVERE, "Error sending email", e);
            showAlert("Failed to send email. Please try again.", Alert.AlertType.ERROR, "red");
        } catch (Exception e) {
            //Logger.getLogger(SendEmail.class.getName()).log(Level.SEVERE, "Error sending email", e);
            showAlert("Error : " + e.getMessage(), Alert.AlertType.ERROR, "red");
        }
    }

    private Message prepareMessage(Session session, String myAccountEmail, String recipientEmail, String ccEmail, String bccEmail, String msgSubject, String msgContent, String qrCodeFilePath) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(myAccountEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));

            if (ccEmail != null && !ccEmail.isEmpty()) {
                message.setRecipient(Message.RecipientType.CC, new InternetAddress(ccEmail));
            }
            if (bccEmail != null && !bccEmail.isEmpty()) {
                message.setRecipient(Message.RecipientType.BCC, new InternetAddress(bccEmail));
            }

            message.setSubject(msgSubject);

            MimeBodyPart htmlBodyPart = new MimeBodyPart();
            htmlBodyPart.setContent(msgContent, "text/html; charset=utf-8");

            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            File qrCodeFile = new File(qrCodeFilePath);
            if (qrCodeFile.exists()) {
                attachmentBodyPart.attachFile(qrCodeFile);
            } else {
                Logger.getLogger(SendEmail.class.getName()).log(Level.WARNING, "Attachment file not found: " + qrCodeFilePath);
                showAlert("Attachment file not found: " + qrCodeFilePath, Alert.AlertType.WARNING, "red");
                return null;
            }

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(htmlBodyPart);
            multipart.addBodyPart(attachmentBodyPart);

            message.setContent(multipart);

            return message;
        } catch (Exception e) {
            Logger.getLogger(SendEmail.class.getName()).log(Level.SEVERE, "Error preparing message", e);
            return null;
        }
    }

    private void showAlert(String message, Alert.AlertType alertType, String color) {
        Alert alert = new Alert(alertType);
        alert.initStyle(UNDECORATED);
        alert.setHeaderText(null);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.getDialogPane().setStyle("-fx-background-color: " + color + "; -fx-border-color: #666464; " +
                "-fx-border-width: 3;" +
                "-fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment: center; -fx-padding: 10;");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
