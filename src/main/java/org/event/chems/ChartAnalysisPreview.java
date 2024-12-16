package org.event.chems;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.InputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import static javafx.stage.StageStyle.UNDECORATED;

public class ChartAnalysisPreview  {

    private static Connection getConnection() throws SQLException, ClassNotFoundException {
        DatabaseServerAndDriver asd1 = new DatabaseServerAndDriver();
        Class.forName(asd1.getDB_DRIVER()); // Load the driver
        return DriverManager.getConnection(asd1.getDB_URL(), asd1.getDB_USER(), asd1.getDB_PASSWORD()); // Return connection
    }

    public ChartAnalysisPreview() {
        initializeStage();
        createSummaryStatistics();
        createUserTypeDistributionChart();
        createPaymentStatusChart();
        createRevenueBarChart();
        createRegistrationTrendChart();
    }

    public void initializeStage() {
        Stage primaryStage = new Stage();
        primaryStage.setTitle("Christmas Event Management System Dashboard");

        InputStream imageStream = getClass().getResourceAsStream("/images/image_login2.png");

        if (imageStream != null) { // Check if the image is found
            Image icon = new Image(imageStream);
            primaryStage.getIcons().add(icon);
        } else {
            System.out.println("Image not found.");
            showAlert("Image not found.", Alert.AlertType.ERROR);
        }

        HBox summaryBox = createSummaryStatistics();
        PieChart userTypeChart = createUserTypeDistributionChart();
        PieChart paymentStatusChart = createPaymentStatusChart();
        BarChart<String, Number> barChart = createRevenueBarChart();
        LineChart<String, Number> lineChart = createRegistrationTrendChart();

        styleComponents(summaryBox, userTypeChart, paymentStatusChart, barChart, lineChart);

        HBox topCharts = new HBox(20, userTypeChart, paymentStatusChart);
        HBox.setHgrow(userTypeChart, Priority.ALWAYS);
        HBox.setHgrow(paymentStatusChart, Priority.ALWAYS);

        HBox middleCharts = new HBox(20, barChart, lineChart);
        HBox.setHgrow(barChart, Priority.ALWAYS);
        HBox.setHgrow(lineChart, Priority.ALWAYS);

        VBox mainLayout = new VBox(20, topCharts, middleCharts, summaryBox);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(mainLayout, 1200, 900);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void styleComponents(HBox summaryBox, PieChart userTypeChart, PieChart paymentStatusChart,
                                 BarChart<String, Number> barChart, LineChart<String, Number> lineChart) {
        summaryBox.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-border-color: black; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-padding: 10px; -fx-effect: dropshadow(one-pass-box, rgba(0, 0, 0, 0.25), 10, 0.1, 0, 0);");
        userTypeChart.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-border-color: black; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-padding: 10px; -fx-effect: dropshadow(one-pass-box, rgba(0, 0, 0, 0.25), 10, 0.1, 0, 0);");
        paymentStatusChart.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-border-color: black; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-padding: 10px; -fx-effect: dropshadow(one-pass-box, rgba(0, 0, 0, 0.25), 10, 0.1, 0, 0);");
        barChart.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-border-color: black; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-padding: 10px; -fx-effect: dropshadow(one-pass-box, rgba(0, 0, 0, 0.25), 10, 0.1, 0, 0);");
        lineChart.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-border-color: black; -fx-border-width: 2px; -fx-border-radius: 10px; -fx-padding: 10px; -fx-effect: dropshadow(one-pass-box, rgba(0, 0, 0, 0.25), 10, 0.1, 0, 0);");

        userTypeChart.setPrefSize(500, 400);
        paymentStatusChart.setPrefSize(500, 400);
        barChart.setPrefSize(500, 400);
        lineChart.setPrefSize(500, 400);
    }

    private HBox createSummaryStatistics() {
        HBox summaryBox = new HBox(20);
        summaryBox.setPadding(new Insets(20));
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.setStyle("-fx-background-color: #f4f4f4; "
                + "-fx-border-color: red; "
                + "-fx-border-width: 2px; "
                + "-fx-border-radius: 10px; "
                + "-fx-background-radius: 10px;");

        String[] queries = {
                "SELECT COUNT(*) FROM chemsuser",
                "SELECT SUM(price) FROM chemsuser WHERE payment = 'Paid'",
                "SELECT COUNT(*) FROM chemsuser WHERE payment = 'Pending'",
                "SELECT COUNT(*) FROM chemsuser WHERE payment = 'Free'"
        };

        String[] labels = {
                "Total Registrations",
                "Total Revenue (Rs.)",
                "Pending Payments",
                "Free Registrations"
        };

        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement()) {

            for (int i = 0; i < queries.length; i++) {
                ResultSet rs = stmt.executeQuery(queries[i]);
                String value = rs.next() ? rs.getString(1) : "-";

                String formattedValue = String.format("%.0f", Double.parseDouble(value));

                VBox card = createStatCard(labels[i], formattedValue);
                HBox.setHgrow(card, Priority.ALWAYS);
                card.setMaxWidth(Double.MAX_VALUE);

                summaryBox.getChildren().add(card);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("Error loading statistics");
            errorLabel.setStyle("-fx-text-fill: red;");
            summaryBox.getChildren().add(errorLabel);
        }

        addFadeTransition(summaryBox);
        return summaryBox;
    }

    private VBox createStatCard(String title, String value) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.CENTER);

        card.setStyle("-fx-background-color: linear-gradient(to bottom, #E3F2FD, #BBDEFB);"
                + "-fx-background-radius: 15px;"
                + "-fx-border-radius: 15px;"
                + "-fx-border-color: #64B5F6;"
                + "-fx-border-width: 2px;"
                + "-fx-effect: dropshadow(one-pass-box, rgba(0, 0, 0, 0.15), 10, 0.0, 0, 4);");

        Text titleText = new Text(title);
        titleText.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        titleText.setFill(Color.web("#B22222"));

        Text valueText = new Text(value);
        valueText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        valueText.setFill(Color.web("#0D47A1"));

        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: linear-gradient(to bottom, #BBDEFB, #90CAF9);"
                + "-fx-background-radius: 15px;"
                + "-fx-border-radius: 15px;"
                + "-fx-border-color: #42A5F5;"
                + "-fx-border-width: 2px;"
                + "-fx-effect: dropshadow(one-pass-box, rgba(0, 0, 0, 0.3), 15, 0.0, 0, 6);")); // Stronger shadow on hover

        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: linear-gradient(to bottom, #E3F2FD, #BBDEFB);"
                + "-fx-background-radius: 15px;"
                + "-fx-border-radius: 15px;"
                + "-fx-border-color: #64B5F6;"
                + "-fx-border-width: 2px;"
                + "-fx-effect: dropshadow(one-pass-box, rgba(0, 0, 0, 0.15), 10, 0.0, 0, 4);")); // Original shadow

        card.getChildren().addAll(titleText, valueText);
        return card;
    }

    private void addFadeTransition(HBox summaryBox) {
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), summaryBox);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        fadeTransition.play();
    }

    private PieChart createUserTypeDistributionChart() {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("User Type Distribution");
        pieChart.setStyle("-fx-pie-label-visible: true;");

        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT user_type, COUNT(*) as count FROM chemsuser GROUP BY user_type");

            while (rs.next()) {
                String userType = rs.getString("user_type");
                int count = rs.getInt("count");
                PieChart.Data slice = new PieChart.Data(userType + " (" + count + ")", count);
                pieChart.getData().add(slice);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        pieChart.getData().forEach(data -> {
            data.getNode().setOnMouseEntered(e -> data.getNode().setStyle("-fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
            data.getNode().setOnMouseExited(e -> data.getNode().setStyle("-fx-scale-x: 1; -fx-scale-y: 1;"));
        });

        return pieChart;
    }

    private PieChart createPaymentStatusChart() {
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Payment Status Distribution");

        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement()) {

            ResultSet rs = stmt.executeQuery("SELECT payment, COUNT(*) as count FROM chemsuser GROUP BY payment");

            while (rs.next()) {
                String payment = rs.getString("payment");
                int count = rs.getInt("count");
                PieChart.Data slice = new PieChart.Data(payment + " (" + count + ")", count);
                pieChart.getData().add(slice);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        pieChart.getData().forEach(data -> {
            data.getNode().setOnMouseEntered(e -> data.getNode().setStyle("-fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
            data.getNode().setOnMouseExited(e -> data.getNode().setStyle("-fx-scale-x: 1; -fx-scale-y: 1;"));
        });

        return pieChart;
    }

    private BarChart<String, Number> createRevenueBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Price Categories");
        yAxis.setLabel("Number of Users");

        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(Number object) {
                return String.format("%,d", object.intValue());
            }
        });

        yAxis.setAutoRanging(true);
        yAxis.setMinorTickVisible(false);

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Price Distribution");

        barChart.setAnimated(false);
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Price Categories");

        try (Connection connection = getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                     "SELECT price, COUNT(*) as user_count " +
                             "FROM chemsuser " +
                             "WHERE price IS NOT NULL " +
                             "GROUP BY price " +
                             "ORDER BY price")) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String priceStr = rs.getString("price"); // Get price as string first
                int userCount = rs.getInt("user_count");
                String priceLabel;
                if (priceStr.equalsIgnoreCase("FREE")) {
                    priceLabel = "FREE";
                } else {
                    try {
                        double price = Double.parseDouble(priceStr);
                        priceLabel = String.format("Rs.%,.2f", price);
                    } catch (NumberFormatException e) {
                        // If parsing fails, use the original string
                        priceLabel = priceStr;
                    }
                }
                XYChart.Data<String, Number> data = new XYChart.Data<>(priceLabel, userCount);
                series.getData().add(data);
                addDataLabel(data);
            }

        } catch (SQLException e) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Database error", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        barChart.getData().add(series);
        return barChart;
    }

    private void addDataLabel(XYChart.Data<String, Number> data) {
        data.nodeProperty().addListener((observable, oldNode, newNode) -> {

            if (newNode != null) {
                StackPane bar = (StackPane) newNode;
                Label label = new Label(String.format("%,d", data.getYValue().intValue()));
                label.getStyleClass().add("chart-data-label");
                label.setStyle(
                        "-fx-font-size: 11px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-text-fill: #555555;" +
                                "-fx-background-radius: 0px;" +
                                "-fx-background-color: white;" +
                                "-fx-padding: 2px;" +
                                "-fx-border-color: #3498db;" +
                                "-fx-border-width: 2px;" +
                                "-fx-border-radius: 0px;"
                );
                label.setTranslateY(-20);
                bar.getChildren().add(label);
            }

        });
    }

    private LineChart<String, Number> createRegistrationTrendChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Registration Date (MM-dd)");
        yAxis.setLabel("Number of Registrations");

        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(Number object) {
                return String.format("%d", object.intValue());
            }
        });
        yAxis.setTickUnit(1);

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Registration Trend");
        XYChart.Series<String, Number> dailySeries = new XYChart.Series<>();
        dailySeries.setName("Daily Registrations");
        XYChart.Series<String, Number> cumulativeSeries = new XYChart.Series<>();
        cumulativeSeries.setName("Cumulative Registrations");

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd");

        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(
                    "SELECT DATE(registerDT) as reg_date, " +
                            "COUNT(*) as daily_count, " +
                            "SUM(COUNT(*)) OVER (ORDER BY DATE(registerDT)) as cumulative_count " +
                            "FROM chemsuser " +
                            "GROUP BY DATE(registerDT) " +
                            "ORDER BY reg_date");

            while (rs.next()) {
                String date = dateFormat.format(rs.getDate("reg_date"));
                int dailyCount = rs.getInt("daily_count");
                int cumulativeCount = rs.getInt("cumulative_count");
                XYChart.Data<String, Number> dailyData = new XYChart.Data<>(date, dailyCount);
                XYChart.Data<String, Number> cumulativeData = new XYChart.Data<>(date, cumulativeCount);
                addDataLabel(dailyData, dailyCount);
                addDataLabel(cumulativeData, cumulativeCount);
                dailySeries.getData().add(dailyData);
                cumulativeSeries.getData().add(cumulativeData);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        lineChart.getData().addAll(dailySeries, cumulativeSeries);
        lineChart.setCreateSymbols(true);
        lineChart.getData().forEach(series -> {
            series.getData().forEach(data -> {
                Node symbol = data.getNode();
                symbol.setStyle(
                        "-fx-font-size: 11px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-text-fill: #555555;" +
                                "-fx-background-radius: 200px;" +
                                "-fx-background-color: white;" +
                                "-fx-padding: 2px;" +
                                "-fx-border-color: #3498db;" +
                                "-fx-border-width: 2px;" +
                                "-fx-border-radius: 200px;"
                );
            });
        });

        lineChart.setAnimated(false);
        xAxis.setTickLabelRotation(1);
        return lineChart;
    }

    private void addDataLabel(XYChart.Data<String, Number> data, int value) {
        data.nodeProperty().addListener((observable, oldNode, newNode) -> {
            if (newNode != null) {
                Label label = new Label(String.valueOf(value));
                label.setStyle("-fx-font-size: 10px; -fx-text-fill: black;");
                StackPane node = (StackPane) newNode;
                node.getChildren().add(label);
            }
        });
    }

    private void showAlert(String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.initStyle(UNDECORATED);
        alert.setHeaderText(null);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.getDialogPane().setStyle("-fx-background-color: white;" +
                "-fx-border-color: #666464; -fx-border-width: 3; " +
                "-fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-alignment: center; -fx-padding: 10;");
        alert.setContentText(message);
        alert.showAndWait();
    }

}

