package com.group27.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.group27.core.DatabaseAdapter;
import com.group27.model.Product;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

    public static ObservableList<CartItem> getCartItems() { return cartItems; }

    public static void addItem(Product p, double amount, double price) {
        for (CartItem item : cartItems) {
            if (item.getProduct().getId() == p.getId()) {
                item.addAmount(amount);
                // Trigger listener by removing and re-adding to ensure change is detected
                int idx = cartItems.indexOf(item);
                cartItems.remove(idx);
                cartItems.add(idx, item);
                return;
            }
        }
        cartItems.add(new CartItem(p, amount, price));
    }
    
    public static void clearCart() {
        cartItems.clear();
    }

    @FXML private ListView<CartItem> cartListView;
    @FXML private DatePicker deliveryDate;
    @FXML private javafx.scene.control.Spinner<Integer> hourSpinner;
    @FXML private javafx.scene.control.Spinner<Integer> minuteSpinner;
    @FXML private Label totalLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label discountLabel;
    @FXML private TextField couponField;
    @FXML private Button applyCouponBtn;
    @FXML private Button removeCouponBtn;
    @FXML private javafx.scene.layout.HBox couponStatusBox;
    @FXML private Label appliedCouponLabel;
    
    private String appliedCouponCode = null;
    
    private double discountAmount = 0.0;
    private final double MIN_CART_VALUE = 10.0;

    @FXML
    public void initialize() {
        System.out.println("DEBUG: CartController initialized. Cart items count: " + cartItems.size());
        
        // Initialize time spinners
        if (hourSpinner != null) {
            javafx.scene.control.SpinnerValueFactory<Integer> hourFactory = 
                new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 12);
            hourSpinner.setValueFactory(hourFactory);
            hourSpinner.setEditable(true);
        }
        
        if (minuteSpinner != null) {
            javafx.scene.control.SpinnerValueFactory<Integer> minuteFactory = 
                new javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0, 15);
            minuteSpinner.setValueFactory(minuteFactory);
            minuteSpinner.setEditable(true);
        }
        
        if (cartListView == null) {
            System.out.println("ERROR: cartListView is NULL!");
            return;
        }
        
        cartListView.setItems(cartItems);
        System.out.println("DEBUG: cartListView.setItems called with " + cartItems.size() + " items");
        
        // Force refresh
        cartListView.refresh();
        cartListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    javafx.scene.layout.HBox card = new javafx.scene.layout.HBox(15);
                    card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    card.getStyleClass().add("liquid-glass-pane");
                    card.setStyle("-fx-padding: 10; -fx-background-color: rgba(255,255,255,0.9);");

                    // Image
                    javafx.scene.image.ImageView img = new javafx.scene.image.ImageView();
                    img.setFitHeight(50);
                    img.setFitWidth(50);
                    img.setPreserveRatio(true);

                    java.io.InputStream is = DatabaseAdapter.getInstance().getProductImage(item.getProduct().getId());
                    if (is != null) img.setImage(new javafx.scene.image.Image(is));

                    // Info
                    javafx.scene.layout.VBox info = new javafx.scene.layout.VBox(5);
                    Label nameLbl = new Label(item.getProduct().getName());
                    nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
                    Label priceLbl = new Label("$" + String.format("%.2f", item.getPriceAtMoment()) + " / kg");
                    info.getChildren().addAll(nameLbl, priceLbl);

                    // Controls
                    javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
                    javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                    // Quantity controls
                    javafx.scene.layout.HBox qtyBox = new javafx.scene.layout.HBox(5);
                    qtyBox.setAlignment(javafx.geometry.Pos.CENTER);
                    
                    Button minusBtn = new Button("-");
                    minusBtn.getStyleClass().add("qty-button");
                    minusBtn.setOnAction(e -> {
                        if (item.getAmount() > 0.25) {
                            item.addAmount(-0.25);
                            int idx = cartItems.indexOf(item);
                            cartItems.remove(idx);
                            cartItems.add(idx, item);
                            updateTotal();
                        }
                    });
                    
                    Label amountLbl = new Label(String.format("%.2f kg", item.getAmount()));
                    amountLbl.setStyle("-fx-font-size: 14px; -fx-min-width: 60; -fx-alignment: center;");
                    
                    Button plusBtn = new Button("+");
                    plusBtn.getStyleClass().add("qty-button");
                    plusBtn.setOnAction(e -> {
                        // Check if we can add more (need to implement stock check)
                        item.addAmount(0.25);
                        int idx = cartItems.indexOf(item);
                        cartItems.remove(idx);
                        cartItems.add(idx, item);
                        updateTotal();
                    });
                    
                    qtyBox.getChildren().addAll(minusBtn, amountLbl, plusBtn);

                    Label totalLbl = new Label("$" + String.format("%.2f", item.getTotal()));
                    totalLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef6c00;");

                    Button removeBtn = new Button("X");
                    removeBtn.setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828; -fx-background-radius: 15;");
                    removeBtn.setOnAction(e -> {
                        cartItems.remove(item);
                        updateTotal();
                    });

                    card.getChildren().addAll(img, info, spacer, qtyBox, new Label("="), totalLbl, removeBtn);
                    setGraphic(card);
                }
            }
        });
        
        updateTotal();
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
        // Check if a coupon is already applied
        if (appliedCouponCode != null) {
            showAlert("Coupon Already Applied", 
                "You can only use one coupon per order.\nPlease remove the current coupon (" + appliedCouponCode + ") to apply a different one.");
            return;
        }
        
        String code = couponField.getText().trim();
        if (code.isEmpty()) {
            showAlert("Error", "Please enter a coupon code.");
            return;
        }
        
        com.group27.model.User user = com.group27.utils.UserSession.getInstance().getCurrentUser();
        if (user == null) {
            showAlert("Error", "Please login to use coupons.");
            return;
        }
        
        // Check if user owns this coupon and hasn't used it
        String checkQuery = "SELECT uc.id, c.discount_amount, c.min_spend " +
                           "FROM UserCoupons uc " +
                           "JOIN Coupons c ON uc.coupon_id = c.id " +
                           "WHERE uc.user_id = ? AND c.code = ? AND uc.used = FALSE AND c.active = TRUE";
        
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(checkQuery)) {
            stmt.setInt(1, user.getId());
            stmt.setString(2, code);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                double subtotal = cartItems.stream().mapToDouble(CartItem::getTotal).sum();
                double minSpend = rs.getDouble("min_spend");
                double discount = rs.getDouble("discount_amount");
                
                if (subtotal >= minSpend) {
                    appliedCouponCode = code;
                    discountAmount = discount;
                    updateTotal();
                    
                    // Show coupon status
                    if (couponStatusBox != null) {
                        appliedCouponLabel.setText("✓ " + code + " applied (-$" + String.format("%.2f", discount) + ")");
                        couponStatusBox.setVisible(true);
                        couponStatusBox.setManaged(true);
                        couponField.setDisable(true);
                        applyCouponBtn.setDisable(true);
                    }
                    
                    showAlert("Success!", "Coupon '" + code + "' applied successfully!\nYou saved $" + String.format("%.2f", discount));
                } else {
                    showAlert("Minimum Spend Required", 
                        String.format("This coupon requires a minimum spend of $%.2f.\nYour current subtotal is $%.2f.", minSpend, subtotal));
                }
            } else {
                showAlert("Invalid Coupon", 
                    "This coupon is either invalid, already used, or not available in your account.\n\n" +
                    "Tip: Check 'My Coupons' to see your available coupons!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to apply coupon: " + e.getMessage());
        }
    }
    
    @FXML
    private void removeCoupon() {
        appliedCouponCode = null;
        discountAmount = 0;
        updateTotal();
        
        // Hide coupon status
        if (couponStatusBox != null) {
            couponStatusBox.setVisible(false);
            couponStatusBox.setManaged(false);
            couponField.setDisable(false);
            applyCouponBtn.setDisable(false);
            couponField.clear();
        }
        
        showAlert("Coupon Removed", "The coupon has been removed from your order.");
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
        
        if (date == null) {
            showAlert("Missing Info", "Please select delivery date and time.");
            return;
        }
        
        if (hourSpinner == null || minuteSpinner == null) {
            showAlert("Error", "Time selection not available.");
            return;
        }
        
        try {
            int hour = hourSpinner.getValue();
            int minute = minuteSpinner.getValue();
            LocalTime time = LocalTime.of(hour, minute);
            LocalDateTime deliveryDateTime = LocalDateTime.of(date, time);
            
            System.out.println("DEBUG: Selected Delivery: " + deliveryDateTime);
            System.out.println("DEBUG: Current Time: " + LocalDateTime.now());
            
            // Delivery must be within 48 hours
            if (deliveryDateTime.isAfter(LocalDateTime.now().plusHours(48))) {
                showAlert("Invalid Date", "⚠️ Delivery must be within 48 hours.\n\nPlease select a date and time within the next 2 days.");
                return;
            }
            if (deliveryDateTime.isBefore(LocalDateTime.now().plusHours(1))) {
                showAlert("Invalid Date", "⚠️ Please select a delivery time at least 1 hour from now.\n\nThis allows us time to prepare your order.");
                return;
            }
            
            // Proceed to save order
            saveOrder(deliveryDateTime);
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Failed to process checkout: " + e.getMessage());
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
            int pointsEarned = (int)total;
            updateLoyaltyPoints(userId, pointsEarned);
            
            // Update user session with new points
            com.group27.model.User currentUser = com.group27.utils.UserSession.getInstance().getCurrentUser();
            if (currentUser != null) {
                currentUser.setLoyaltyPoints(currentUser.getLoyaltyPoints() + pointsEarned);
            }
            
            showAlert("Success", "Order placed successfully! Invoice generated.\n🎁 You earned " + pointsEarned + " loyalty points!");
            clearCart();
            ((Stage) cartListView.getScene().getWindow()).close();
            
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
