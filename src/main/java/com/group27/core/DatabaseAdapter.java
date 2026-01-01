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
        initializeDatabase();
    }

    public static DatabaseAdapter getInstance() {
        if (instance == null) {
            instance = new DatabaseAdapter();
        }
        return instance;
    }

    private void connect() {
        try {
            // First connect without DB to create it if not exists
            try (Connection setupConn = DriverManager.getConnection(BASE_URL, DB_USER, DB_PASS);
                 Statement stmt = setupConn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            }

            // Connect to the specific DB
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            System.out.println("Veritabanı bağlantısı başarılı!");
        } catch (SQLException e) {
            System.err.println("Veritabanı bağlantı hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeDatabase() {
        try (Statement stmt = getConnection().createStatement()) {
            // UserInfo table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS UserInfo (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(50) NOT NULL, " +
                    "role VARCHAR(20) NOT NULL, " +
                    "address VARCHAR(255), " +
                    "loyalty_points INT DEFAULT 0)");

            // Coupons table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Coupons (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "code VARCHAR(20) UNIQUE, " +
                    "discount_amount DOUBLE, " +
                    "min_spend DOUBLE, " +
                    "active BOOLEAN DEFAULT TRUE)");

            // ProductInfo table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS ProductInfo (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(50) NOT NULL, " +
                    "type VARCHAR(20) NOT NULL, " +
                    "price DOUBLE NOT NULL, " +
                    "stock DOUBLE NOT NULL, " +
                    "threshold DOUBLE NOT NULL, " +
                    "imagelocation BLOB)");

            // OrderInfo table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS OrderInfo (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "ordertime DATETIME, " +
                    "deliverytime DATETIME, " +
                    "products TEXT, " + // Storing as JSON or simple text list for now (CLOB-like)
                    "user_id INT, " +
                    "carrier_id INT, " +
                    "isdelivered BOOLEAN DEFAULT FALSE, " +
                    "totalcost DOUBLE, " +
                    "invoice MEDIUMTEXT, " + // CLOB for invoice
                    "carrier_rating INT DEFAULT 0, " +
                    "FOREIGN KEY (user_id) REFERENCES UserInfo(id))");

            // Messages table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS Messages (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "sender_id INT, " +
                    "receiver_id INT, " +
                    "content TEXT, " +
                    "reply TEXT, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (sender_id) REFERENCES UserInfo(id))");

            seedData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void seedData() throws SQLException {
        // Seed Users
        if (!hasData("UserInfo")) {
            String insertUser = "INSERT INTO UserInfo (username, password, role, address) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = getConnection().prepareStatement(insertUser)) {
                // Customer
                pstmt.setString(1, "cust");
                pstmt.setString(2, "cust");
                pstmt.setString(3, "customer");
                pstmt.setString(4, "123 Apple St");
                pstmt.addBatch();
                // Carrier
                pstmt.setString(1, "carr");
                pstmt.setString(2, "carr");
                pstmt.setString(3, "carrier");
                pstmt.setString(4, "Carrier Station");
                pstmt.addBatch();
                // Owner
                pstmt.setString(1, "own");
                pstmt.setString(2, "own");
                pstmt.setString(3, "owner");
                pstmt.setString(4, "HQ");
                pstmt.addBatch();
                pstmt.executeBatch();
            }
        }

        // Seed Coupons
        if (!hasData("Coupons")) {
            String insertCoupon = "INSERT INTO Coupons (code, discount_amount, min_spend) VALUES (?, ?, ?)";
            try (PreparedStatement pstmt = getConnection().prepareStatement(insertCoupon)) {
                 pstmt.setString(1, "WELCOME2025");
                 pstmt.setDouble(2, 10.0);
                 pstmt.setDouble(3, 50.0);
                 pstmt.executeUpdate();
            }
        }

        // Seed Products
        if (!hasData("ProductInfo")) {
            String insertProduct = "INSERT INTO ProductInfo (name, type, price, stock, threshold, imagelocation) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = getConnection().prepareStatement(insertProduct)) {
                String[] veg = {"Tomato", "Potato", "Onion", "Carrot", "Cucumber", "Pepper", "Lettuce", "Spinach", "Broccoli", "Cauliflower", "Garlic", "Zucchini"};
                String[] fruit = {"Apple", "Banana", "Orange", "Grape", "Strawberry", "Watermelon", "Melon", "Peach", "Pear", "Cherry", "Plum", "Kiwi"};

                for (String v : veg) {
                    pstmt.setString(1, v);
                    pstmt.setString(2, "Vegetable");
                    pstmt.setDouble(3, 2.0 + (Math.random() * 5)); // Random price
                    pstmt.setDouble(4, 100.0);
                    pstmt.setDouble(5, 10.0);
                    byte[] img = generateImage(v, Color.GREEN);
                    pstmt.setBytes(6, img);
                    pstmt.addBatch();
                }
                for (String f : fruit) {
                    pstmt.setString(1, f);
                    pstmt.setString(2, "Fruit");
                    pstmt.setDouble(3, 3.0 + (Math.random() * 10));
                    pstmt.setDouble(4, 100.0);
                    pstmt.setDouble(5, 10.0);
                    byte[] img = generateImage(f, Color.ORANGE);
                    pstmt.setBytes(6, img);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
        } else {
             // Update images if null
             updateMissingImages();
        }
    }
    
    private void updateMissingImages() {
        String query = "SELECT id, name, type FROM ProductInfo WHERE imagelocation IS NULL";
        String update = "UPDATE ProductInfo SET imagelocation = ? WHERE id = ?";
        try (Statement stmt = getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(query);
             PreparedStatement pstmt = getConnection().prepareStatement(update)) {
             
             while (rs.next()) {
                 int id = rs.getInt("id");
                 String name = rs.getString("name");
                 String type = rs.getString("type");
                 Color c = type.equalsIgnoreCase("Vegetable") ? Color.GREEN : Color.ORANGE;
                 byte[] img = generateImage(name, c);
                 pstmt.setBytes(1, img);
                 pstmt.setInt(2, id);
                 pstmt.executeUpdate();
             }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private byte[] generateImage(String text, Color bgColor) {
        int width = 200;
        int height = 200;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        g2d.setColor(bgColor);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        
        java.awt.FontMetrics fm = g2d.getFontMetrics();
        int x = (width - fm.stringWidth(text)) / 2;
        int y = (height - fm.getHeight()) / 2 + fm.getAscent();
        
        g2d.drawString(text, x, y);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", baos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
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
