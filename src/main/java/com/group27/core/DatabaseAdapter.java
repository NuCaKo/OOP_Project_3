package com.group27.core;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import javax.imageio.ImageIO;

public class DatabaseAdapter {
    // PDF[cite: 67]: Tüm gruplar bu giriş bilgilerini kullanacak.
    private static final String BASE_URL = "jdbc:mysql://localhost:3306/";
    private static final String DB_NAME = "greengrocer_db";
    private static final String DB_URL = BASE_URL + DB_NAME;
    private static final String DB_USER = "root";
    private static final String DB_PASS = "pass1234";

    private Connection connection;

    // Singleton Pattern: Uygulama boyunca tek bir bağlantı nesnesi olsun
    private static DatabaseAdapter instance;

    private DatabaseAdapter() {
        connect();
        // Database initialization is handled by database.sql
        // We only ensure images are loaded if missing because BLOBs are hard to seed via SQL script
        refreshAllProductImages();
    }

    public static DatabaseAdapter getInstance() {
        if (instance == null) {
            instance = new DatabaseAdapter();
        }
        return instance;
    }

    private void connect() {
        try {
            // Connect to the specific DB
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Veritabanı bağlantısı başarılı!");
        } catch (SQLException e) {
            System.err.println("Veritabanı bağlantı hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void refreshAllProductImages() {
        String query = "SELECT id, name, type FROM ProductInfo";
        String update = "UPDATE ProductInfo SET imagelocation = ? WHERE id = ?";
        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query);
             PreparedStatement pstmt = conn.prepareStatement(update)) {
             
             while (rs.next()) {
                 int id = rs.getInt("id");
                 String name = rs.getString("name");
                 String type = rs.getString("type");
                 byte[] img = generateProductImage(name, type);
                 pstmt.setBytes(1, img);
                 pstmt.setInt(2, id);
                 pstmt.executeUpdate();
             }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private byte[] generateProductImage(String name, String type) {
        String filename = "default.png";
        String lower = name.toLowerCase();
        
        if (lower.contains("tomato")) filename = "tomato.png";
        else if (lower.contains("potato")) filename = "potato.png";
        else if (lower.contains("onion")) filename = "onion.png";
        else if (lower.contains("carrot")) filename = "carrot.png";
        else if (lower.contains("cucumber") || lower.contains("zucchini")) filename = "cucumber.png";
        else if (lower.contains("pepper")) filename = "pepper.png";
        else if (lower.contains("lettuce")) filename = "lettuce.png";
        else if (lower.contains("spinach")) filename = "spinach.png";
        else if (lower.contains("broccoli")) filename = "broccoli.png";
        else if (lower.contains("garlic")) filename = "garlic.png";
        else if (lower.contains("apple")) filename = "apple.png";
        else if (lower.contains("banana")) filename = "banana.png";
        else if (lower.contains("orange")) filename = "orange.png";
        else if (lower.contains("grape")) filename = "grape.png";
        else if (lower.contains("strawberry")) filename = "strawberry.png";
        else if (lower.contains("watermelon")) filename = "watermelon.png";
        else if (lower.contains("melon")) filename = "melon.png";
        else if (lower.contains("peach")) filename = "peach.png";
        else if (lower.contains("pear")) filename = "pear.png";
        else if (lower.contains("cherry")) filename = "cherry.png";
        else if (lower.contains("kiwi")) filename = "kiwi.png";

        try (java.io.InputStream is = getClass().getResourceAsStream("/images/" + filename)) {
            if (is != null) {
                return is.readAllBytes();
            } else {
                System.err.println("Image not found: " + filename);
                return new byte[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private boolean hasData(String tableName) throws SQLException {
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // Kullanıcı Giriş Kontrolü (Login)
    public boolean login(String username, String password) {
        String query = "SELECT * FROM UserInfo WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Eğer kayıt varsa true döner
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
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
                    rs.getString("address")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Kullanıcı Rolünü Getir (Customer, Owner, Carrier?)
    public String getUserRole(String username) {
        String query = "SELECT role FROM UserInfo WHERE username = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    // Kullanıcı Kaydı (Register)
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

    // ÖRNEK: Ürün Fiyatını Getirirken "Greedy Owner" Mantığı 
    // Bu metod ürünleri çekerken stok kontrolü yapar.
    // Ekranda gösterilecek fiyatı hesaplar.
    public double calculateDynamicPrice(double basePrice, double stock, double threshold) {
        if (stock <= threshold) {
            return basePrice * 2.0; // Stok azsa fiyatı ikiye katla!
        }
        return basePrice;
    }
    
    public java.io.InputStream getProductImage(int productId) {
        String query = "SELECT imagelocation FROM ProductInfo WHERE id = ?";
        try (PreparedStatement stmt = getConnection().prepareStatement(query)) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Blob blob = rs.getBlob("imagelocation");
                if (blob != null) {
                    // Read all bytes to memory to avoid stream closing issue when stmt closes
                    byte[] bytes = blob.getBytes(1, (int) blob.length());
                    return new java.io.ByteArrayInputStream(bytes);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Bağlantıyı getter ile dışarı açmak (Gerektiğinde diğer sınıflar kullansın)
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
