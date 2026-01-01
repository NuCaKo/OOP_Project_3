package com.group27.controller;

import com.group27.core.DatabaseAdapter;
import com.group27.model.Product;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class CartController {

    public static class CartItem {
        private Product product;
        private double amount;
        private double priceAtMoment;

        public CartItem(Product product, double amount, double priceAtMoment) {
            this.product = product;
            this.amount = amount;
            this.priceAtMoment = priceAtMoment;
        }

        public Product getProduct() { return product; }
        public double getAmount() { return amount; }
        public double getPriceAtMoment() { return priceAtMoment; }
        public void addAmount(double amt) { this.amount += amt; }
        public double getTotal() { return amount * priceAtMoment; }
    }

    private static ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    public static void addItem(Product p, double amount, double price) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId() == p.getId()) {
                item.addAmount(amount);
                // Trigger update hack if needed, or rely on TableView refresh
                int idx = cartItems.indexOf(item);
                cartItems.set(idx, item);
                return;
            }
        }
        cartItems.add(new CartItem(p, amount, price));
    }
    
    public static void clearCart() {
        cartItems.clear();
    }

    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> nameCol;
    @FXML private TableColumn<CartItem, Number> priceCol;
    @FXML private TableColumn<CartItem, Number> amountCol;
    @FXML private TableColumn<CartItem, Number> totalCol;
    @FXML private DatePicker deliveryDate;
    @FXML private TextField deliveryTime;
    @FXML private Label totalLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label discountLabel;
    @FXML private TextField couponField;
    
    private double discountAmount = 0.0;
    private final double MIN_CART_VALUE = 10.0;

    @FXML
    public void initialize() {
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProduct().getName()));
        priceCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPriceAtMoment()));
        amountCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getAmount()));
        totalCol.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getTotal()));

        cartTable.setItems(cartItems);
        
        updateTotal();
        
        // Listener for updates
        // In a real app, bind properties. Here simpler.
        cartItems.addListener((javafx.collections.ListChangeListener<CartItem>) c -> updateTotal());
    }

    private void updateTotal() {
        double subtotal = cartItems.stream().mapToDouble(CartItem::getTotal).sum();
        double vat = subtotal * 0.18;
        
        // Apply Loyalty Check (10% if points > 100) - Simple Logic
        // In a real app, we fetch user points.
        // Assuming we fetched it in Session or query.
        
        double total = subtotal + vat - discountAmount;
        if (total < 0) total = 0;
        
        subtotalLabel.setText("Subtotal: $" + String.format("%.2f", subtotal));
        taxLabel.setText("VAT (18%): $" + String.format("%.2f", vat));
        discountLabel.setText("Discount: -$" + String.format("%.2f", discountAmount));
        totalLabel.setText("Total: $" + String.format("%.2f", total));
    }
    
    @FXML
    private void applyCoupon() {
        String code = couponField.getText();
        if (code.equals("WELCOME2025")) { // Hardcoded for simplicity or DB check
            discountAmount = 10.0;
            updateTotal();
            showAlert("Success", "Coupon Applied: $10.00 off");
        } else {
            // DB check for coupons
            String query = "SELECT discount_amount, min_spend FROM Coupons WHERE code = ? AND active = TRUE";
            Connection conn = DatabaseAdapter.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, code);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    double subtotal = cartItems.stream().mapToDouble(CartItem::getTotal).sum();
                    if (subtotal >= rs.getDouble("min_spend")) {
                        discountAmount = rs.getDouble("discount_amount");
                        updateTotal();
                        showAlert("Success", "Coupon Applied!");
                    } else {
                        showAlert("Error", "Minimum spend not met.");
                    }
                } else {
                    showAlert("Error", "Invalid Coupon");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleCheckout() {
        if (cartItems.isEmpty()) {
            showAlert("Cart Empty", "Add items before checkout.");
            return;
        }
        
        // Check minimum cart value
        double subtotal = cartItems.stream().mapToDouble(CartItem::getTotal).sum();
        if (subtotal < MIN_CART_VALUE) {
            showAlert("Error", "Minimum cart value of $" + MIN_CART_VALUE + " not met.");
            return;
        }
        
        LocalDate date = deliveryDate.getValue();
        String timeStr = deliveryTime.getText();
        
        if (date == null || timeStr.isEmpty()) {
            showAlert("Missing Info", "Please select delivery date and time.");
            return;
        }
        
        try {
            LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime deliveryDateTime = LocalDateTime.of(date, time);
            
            System.out.println("DEBUG: Selected Delivery: " + deliveryDateTime);
            System.out.println("DEBUG: Current Time: " + LocalDateTime.now());
            
            if (deliveryDateTime.isAfter(LocalDateTime.now().plusHours(168))) { // Relaxed to 1 week
                showAlert("Invalid Date", "Delivery must be within 7 days.");
                return;
            }
            if (deliveryDateTime.isBefore(LocalDateTime.now())) {
                showAlert("Invalid Date", "Time machine broken. Select future date.");
                return;
            }
            
            // Proceed to save order
            saveOrder(deliveryDateTime);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Invalid Time", "Format HH:mm (e.g., 14:30)");
        }
    }
    
    private void saveOrder(LocalDateTime deliveryTime) {
        DatabaseAdapter db = DatabaseAdapter.getInstance();
        com.group27.model.User user = com.group27.utils.UserSession.getInstance().getCurrentUser();
        int userId = (user != null) ? user.getId() : 0;
        
        System.out.println("DEBUG: Saving order for UserID: " + userId);
        
        if (userId == 0) {
            showAlert("Error", "User not logged in. Cannot checkout.");
            return;
        }

        double subtotal = cartItems.stream().mapToDouble(CartItem::getTotal).sum();
        double vat = subtotal * 0.18;
        double total = subtotal + vat - discountAmount;
        if (total < 0) total = 0;
        
        // Serialize products to simple string (JSON-like)
        StringBuilder sb = new StringBuilder();
        for (CartItem item : cartItems) {
            sb.append(item.getProduct().getName())
              .append(":")
              .append(item.getAmount())
              .append(";");
        }
        
        String insertOrder = "INSERT INTO OrderInfo (ordertime, deliverytime, products, user_id, totalcost, isdelivered) \n" +
                "VALUES (NOW(), ?, ?, ?, ?, 0)";
        String updateStock = "UPDATE ProductInfo SET stock = stock - ? WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = db.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // 1. Insert Order
            int orderId = 0;
            try (PreparedStatement stmt = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setObject(1, deliveryTime);
                stmt.setString(2, sb.toString());
                stmt.setInt(3, userId);
                stmt.setDouble(4, total);
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) throw new SQLException("Creating order failed, no rows affected.");
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        orderId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating order failed, no ID obtained.");
                    }
                }
            }

            // 2. Update Stock
            try (PreparedStatement stockStmt = conn.prepareStatement(updateStock)) {
                for (CartItem item : cartItems) {
                    stockStmt.setDouble(1, item.getAmount());
                    stockStmt.setInt(2, item.getProduct().getId());
                    stockStmt.executeUpdate();
                }
            }
            
            conn.commit(); // Commit Transaction
            
            // 3. Post-Transaction Actions (Invoice & Loyalty)
            // Generate Invoice (Connection managed internally or passed? InvoiceGenerator uses new connection currently. It's fine.)
            if (orderId > 0) {
                com.group27.utils.InvoiceGenerator.generateAndSaveInvoice(orderId, cartItems, total);
            }
            
            // Update Loyalty Points
            updateLoyaltyPoints(userId, (int)total);
            
            showAlert("Success", "Order placed successfully! Invoice generated. You earned " + (int)total + " loyalty points.");
            clearCart();
            ((Stage) cartTable.getScene().getWindow()).close();
            
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            showAlert("Error", "Transaction failed: " + e.getMessage());
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); /* Do not close shared connection */ } catch (SQLException ex) { ex.printStackTrace(); }
            }
        }
    }
    
    private void updateLoyaltyPoints(int userId, int points) {
        if (userId <= 0) return;
        String query = "UPDATE UserInfo SET loyalty_points = loyalty_points + ? WHERE id = ?";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, points);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
