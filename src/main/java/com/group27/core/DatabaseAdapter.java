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
                    byte[] img = generateProductImage(v, "Vegetable");
                    pstmt.setBytes(6, img);
                    pstmt.addBatch();
                }
                for (String f : fruit) {
                    pstmt.setString(1, f);
                    pstmt.setString(2, "Fruit");
                    pstmt.setDouble(3, 3.0 + (Math.random() * 10));
                    pstmt.setDouble(4, 100.0);
                    pstmt.setDouble(5, 10.0);
                    byte[] img = generateProductImage(f, "Fruit");
                    pstmt.setBytes(6, img);
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
        }

        // Always refresh images to new style
        refreshAllProductImages();
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
        int width = 250;
        int height = 250;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        // Enable Anti-aliasing
        g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

        // Background (Light/Cream)
        g2d.setColor(new Color(250, 250, 245));
        g2d.fillRect(0, 0, width, height);

        // Determine Color & Shape based on Name
        Color primaryColor = Color.GREEN;
        String shape = "CIRCLE";
        String lowerName = name.toLowerCase();

        if (lowerName.contains("tomato") || lowerName.contains("apple") || lowerName.contains("cherry") || lowerName.contains("strawberry") || lowerName.contains("pepper")) {
            primaryColor = new Color(220, 53, 69); // Red
            if (lowerName.contains("pepper")) shape = "LONG";
        } else if (lowerName.contains("orange") || lowerName.contains("carrot") || lowerName.contains("peach") || lowerName.contains("melon")) {
            primaryColor = new Color(253, 126, 20); // Orange
            if (lowerName.contains("carrot")) shape = "TRIANGLE";
        } else if (lowerName.contains("banana") || lowerName.contains("lemon") || lowerName.contains("corn") || lowerName.contains("potato") || lowerName.contains("pear")) {
            primaryColor = new Color(255, 193, 7); // Yellow
            shape = "OVAL";
            if (lowerName.contains("banana")) shape = "CURVE";
        } else if (lowerName.contains("grape") || lowerName.contains("plum") || lowerName.contains("onion") || lowerName.contains("turnip")) {
            primaryColor = new Color(111, 66, 193); // Purple
        } else if (lowerName.contains("cucumber") || lowerName.contains("zucchini") || lowerName.contains("lettuce") || lowerName.contains("spinach") || lowerName.contains("broccoli") || lowerName.contains("watermelon")) {
            primaryColor = new Color(40, 167, 69); // Green
            if (lowerName.contains("cucumber") || lowerName.contains("zucchini")) shape = "LONG";
            if (lowerName.contains("broccoli") || lowerName.contains("cauliflower")) shape = "CLOUD";
        } else if (lowerName.contains("cauliflower") || lowerName.contains("garlic") || lowerName.contains("mushroom")) {
            primaryColor = new Color(230, 230, 230); // White/Grey
            shape = "CLOUD";
        }

        // Draw Shape
        g2d.setColor(primaryColor);
        if (shape.equals("CIRCLE")) {
            g2d.fillOval(50, 50, 150, 150);
            // Highlight
            g2d.setColor(new Color(255, 255, 255, 100));
            g2d.fillOval(140, 70, 40, 40);
        } else if (shape.equals("OVAL")) {
            g2d.fillOval(75, 50, 100, 150);
        } else if (shape.equals("LONG")) {
            g2d.fillRoundRect(85, 30, 80, 190, 40, 40);
        } else if (shape.equals("CLOUD")) {
            g2d.fillOval(50, 80, 80, 80);
            g2d.fillOval(120, 80, 80, 80);
            g2d.fillOval(85, 50, 80, 80);
        } else if (shape.equals("CURVE")) {
            g2d.setStroke(new java.awt.BasicStroke(40, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g2d.drawArc(50, 50, 150, 150, 180, 135);
        } else if (shape.equals("TRIANGLE")) {
            int[] xPoints = {125, 175, 75};
            int[] yPoints = {220, 40, 40}; // Inverted visually or just triangle
            // Carrot shape: wide top, narrow bottom
             int[] xP = {75, 175, 125};
             int[] yP = {50, 50, 220};
             g2d.fillPolygon(xP, yP, 3);
        }
        
        // Leaf/Stem
        g2d.setColor(new Color(34, 139, 34)); // Forest Green
        if (!shape.equals("CLOUD") && !name.toLowerCase().contains("lettuce")) {
            g2d.fillOval(120, 30, 10, 25);
            g2d.fillOval(120, 30, 25, 10);
        }

        // Text Label (Bottom)
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 22));
        java.awt.FontMetrics fm = g2d.getFontMetrics();
        int textX = (width - fm.stringWidth(name)) / 2;
        int textY = height - 20;

        // Text Shadow
        g2d.setColor(new Color(0, 0, 0, 30));
        g2d.drawString(name, textX + 1, textY + 1);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawString(name, textX, textY);

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
