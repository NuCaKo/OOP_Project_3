package com.group27.core;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAdapter {
    // PDF[cite: 67]: Tüm gruplar bu giriş bilgilerini kullanacak.
    private static final String DB_URL = "jdbc:mysql://localhost:3306/greengrocer_db";
    private static final String DB_USER = "myuser";
    private static final String DB_PASS = "1234";

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
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Veritabanı bağlantısı başarılı!");
        } catch (SQLException e) {
            System.err.println("Veritabanı bağlantı hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Kullanıcı Giriş Kontrolü (Login)
    public boolean login(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Eğer kayıt varsa true döner
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Kullanıcı Rolünü Getir (Customer, Owner, Carrier?)
    public String getUserRole(String username) {
        String query = "SELECT role FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
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

    // ÖRNEK: Ürün Fiyatını Getirirken "Greedy Owner" Mantığı 
    // Bu metod ürünleri çekerken stok kontrolü yapar.
    // Ekranda gösterilecek fiyatı hesaplar.
    public double calculateDynamicPrice(double basePrice, double stock, double threshold) {
        if (stock <= threshold) {
            return basePrice * 2.0; // Stok azsa fiyatı ikiye katla!
        }
        return basePrice;
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