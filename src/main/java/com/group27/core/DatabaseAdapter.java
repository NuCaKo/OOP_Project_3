package com.group27.core;

import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

/**
 * Singleton class that manages database connections and operations.
 * Provides methods for user authentication, product retrieval, and
 * database connection management. Uses MySQL database for data persistence.
 * 
 * @author Group27
 * @version 1.0
 */
public class DatabaseAdapter {
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "greengrocer_db";
    private static final String DB_URL = BASE_URL + DB_NAME;
    private static final String DB_USER = "root";
    private static final String DB_PASS = "pass1234";

    private Connection connection;
    private static DatabaseAdapter instance;
    
    /**
     * Private constructor to enforce singleton pattern.
     * Initializes the database connection upon instantiation.
     */
    private DatabaseAdapter() {
        connect();

    }

    /**
     * Returns the singleton instance of DatabaseAdapter.
     * Creates a new instance if one doesn't exist.
     * 
     * @return The singleton DatabaseAdapter instance
     */
    public static DatabaseAdapter getInstance() {
        if (instance == null) {
            instance = new DatabaseAdapter();
        }
        return instance;
    }

    /**
     * Establishes a connection to the MySQL database.
     * Prints success or error messages to the console.
     */
    private void connect() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Veritabanı bağlantısı başarılı!");
        } catch (SQLException e) {
            System.err.println("Veritabanı bağlantı hatası: " + e.getMessage());
        }
    }

    /**
     * Retrieves the product image from the database as an InputStream.
     * 
     * @param productId The ID of the product whose image is to be retrieved
     * @return InputStream containing the product image, or null if not found or on error
     */
    public InputStream getProductImage(int productId) {
        String query = "SELECT imagelocation FROM ProductInfo WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Blob blob = rs.getBlob("imagelocation");
                if (blob != null) {
                    byte[] bytes = blob.getBytes(1, (int) blob.length());
                    return new ByteArrayInputStream(bytes);
                }
            }
        } catch (SQLException e) {
            System.err.println("Görsel veritabanından alınamadı: " + e.getMessage());
        }
        return null;
    }

    /**
     * Authenticates a user with the provided username and password.
     * 
     * @param username The username to authenticate
     * @param password The password to authenticate
     * @return true if credentials are valid, false otherwise
     */
    public boolean login(String username, String password) {
        String query = "SELECT * FROM UserInfo WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves a User object from the database by username.
     * 
     * @param username The username to search for
     * @return User object if found, null otherwise
     */
    public com.group27.model.User getUserByUsername(String username) {
        String query = "SELECT * FROM UserInfo WHERE username = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new com.group27.model.User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role"),
                        rs.getString("address"),
                        rs.getInt("loyalty_points")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Registers a new user in the database.
     * 
     * @param username The username for the new user
     * @param password The password for the new user
     * @param role The role of the user (customer, carrier, or owner)
     * @param address The address of the user
     * @return true if registration is successful, false otherwise
     */
    public boolean registerUser(String username, String password, String role, String address) {
        String query = "INSERT INTO UserInfo (username, password, role, address) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, role);
            stmt.setString(4, address);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Calculates dynamic pricing based on stock levels.
     * If stock is at or below the threshold, price is doubled.
     * 
     * @param basePrice The base price of the product
     * @param stock The current stock level
     * @param threshold The stock threshold that triggers price increase
     * @return The calculated price (basePrice if stock > threshold, basePrice * 2.0 otherwise)
     */
    public double calculateDynamicPrice(double basePrice, double stock, double threshold) {
        if (stock <= threshold) {
            return basePrice * 2.0;
        }
        return basePrice;
    }

    /**
     * Returns the database connection, reconnecting if necessary.
     * Checks if the connection is null or closed and reconnects if needed.
     * 
     * @return The active database Connection object
     */
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}