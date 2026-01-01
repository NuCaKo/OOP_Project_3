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
