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
 * Handles database connections and interactions.
 * Implements Singleton pattern to manage a shared connection instance.
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
     * Private constructor to prevent direct instantiation.
     * Initializes the database connection.
     */
    private DatabaseAdapter() {
        connect();
    }

    /**
     * Returns the singleton instance of the DatabaseAdapter.
     *
     * @return The singleton instance.
     */
    public static DatabaseAdapter getInstance() {
        if (instance == null) {
            instance = new DatabaseAdapter();
        }
        return instance;
    }

    /**
     * Establishes a connection to the database.
     */
    private void connect() {
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Database connection successful!");
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
        }
    }

    /**
     * Retrieves a product image from the database.
     *
     * @param productId The ID of the product.
     * @return An InputStream containing the image data, or null if not found.
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
            System.err.println("Could not retrieve image from database: " + e.getMessage());
        }
        return null;
    }

    /**
     * Authenticates a user.
     *
     * @param username The username.
     * @param password The hashed password.
     * @return True if credentials are valid, false otherwise.
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
     * Retrieves user details by username.
     *
     * @param username The username to search for.
     * @return A User object if found, null otherwise.
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
     * Registers a new user.
     *
     * @param username The username.
     * @param password The hashed password.
     * @param role     The user's role (customer, carrier, owner).
     * @param address  The user's address.
     * @return True if registration was successful, false otherwise.
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
     * Calculates the dynamic price of a product based on stock levels.
     *
     * @param basePrice The base price of the product.
     * @param stock     The current stock level.
     * @param threshold The threshold below which the price increases.
     * @return The calculated dynamic price.
     */
    public double calculateDynamicPrice(double basePrice, double stock, double threshold) {
        if (stock <= threshold) {
            return basePrice * 2.0;
        }
        return basePrice;
    }

    /**
     * Gets the current database connection. Reconnects if the connection is closed.
     *
     * @return The active Connection object.
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
