package org.event.chems;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseServerAndDriver {

    private String DB_URL = "jdbc:mysql://localhost:3306/christmaseventmanagementsystem";
    private String DB_USER = "admin";
    private String DB_PASSWORD = "admin@123";
    private String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    public String getDB_URL() {
        return DB_URL;
    }

    public String getDB_USER() {
        return DB_USER;
    }

    public String getDB_PASSWORD() {
        return DB_PASSWORD;
    }

    public String getDB_DRIVER() {
        return DB_DRIVER;
    }

    public Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName(DB_DRIVER);
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connection to the database was successful.");
        } catch (ClassNotFoundException e) {
            System.err.println("Database Driver not found. Ensure the JDBC driver is added to the project.");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database. Check the URL, user credentials, and database server status.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected error occurred while connecting to the database.");
            e.printStackTrace();
        }
        return connection;
    }
}
