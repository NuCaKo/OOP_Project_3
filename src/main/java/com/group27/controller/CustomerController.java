package com.group27.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.group27.core.DatabaseAdapter;
import com.group27.model.Product;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CustomerController {

    @FXML private Label welcomeLabel;
    @FXML private TextField searchField;
    @FXML private FlowPane vegPane;
    @FXML private FlowPane fruitPane;
    @FXML private TitledPane vegPaneTitled;
    @FXML private TitledPane fruitPaneTitled;
    @FXML private TextArea messageInput;
    @FXML private ListView<String> messageList;
    @FXML private TextField editAddressField;
    @FXML private PasswordField editPasswordField;
    @FXML private Label loyaltyPointsLabel;

    private List<Product> allProducts = new ArrayList<>();
    
    @FXML
    public void initialize() {
        com.group27.model.User user = com.group27.utils.UserSession.getInstance().getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getUsername());
            if (editAddressField != null) editAddressField.setText(user.getAddress());
            updateLoyaltyPoints();
        } else {
            welcomeLabel.setText("Welcome, Valued Customer");
        }
        
        loadProducts();
        
        // Load messages after a short delay to ensure UI is ready
        javafx.application.Platform.runLater(() -> {
            loadMessages();
            System.out.println("DEBUG: Messages loaded. MessageList null? " + (messageList == null));
            if (messageList != null) {
                System.out.println("DEBUG: MessageList items count: " + messageList.getItems().size());
            }
        });
        
        // Setup message list cell factory for better display
        if (messageList != null) {
            messageList.setCellFactory(param -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setWrapText(true);
                        setStyle("-fx-padding: 8px; -fx-background-color: #f5f5f5; -fx-background-radius: 5px;");
                    }
                }
            });
        }

        // Initialize Mini-Cart
        System.out.println("DEBUG: Initializing mini cart. miniCartList is null: " + (miniCartList == null));
        if (miniCartList != null) {
            // Setup cell factory for better display
            miniCartList.setCellFactory(param -> new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        setWrapText(false);
                        setStyle("-fx-padding: 3px;");
                    }
                }
            });
            
            updateMiniCart();
            // Listen to cart changes
            CartController.getCartItems().addListener((javafx.collections.ListChangeListener.Change<? extends CartController.CartItem> c) -> {
                System.out.println("DEBUG: Cart changed detected!");
                while (c.next()) {
                    if (c.wasAdded() || c.wasRemoved() || c.wasUpdated()) {
                        System.out.println("DEBUG: Cart change: added=" + c.wasAdded() + ", removed=" + c.wasRemoved() + ", updated=" + c.wasUpdated());
                        updateMiniCart();
                        break;
                    }
                }
            });
        } else {
            System.out.println("DEBUG: miniCartList is null, cannot initialize!");
        }
    }

    private void updateMiniCart() {
        if (miniCartList == null) {
            System.out.println("DEBUG: miniCartList is null!");
            return;
        }
        
        System.out.println("DEBUG: Updating mini cart. Cart items: " + CartController.getCartItems().size());
        
        miniCartList.getItems().clear();
        
        if (CartController.getCartItems().isEmpty()) {
            miniCartList.getItems().add("Cart is empty");
            System.out.println("DEBUG: Cart is empty");
            return;
        }
        
        double total = 0;
        for (CartController.CartItem item : CartController.getCartItems()) {
            String itemText = String.format("• %s - %.2f kg × $%.2f = $%.2f", 
                item.getProduct().getName(), 
                item.getAmount(), 
                item.getPriceAtMoment(),
                item.getTotal());
            miniCartList.getItems().add(itemText);
            total += item.getTotal();
            System.out.println("DEBUG: Added to preview: " + itemText);
        }
        miniCartList.getItems().add("━━━━━━━━━━━━━━━━");
        miniCartList.getItems().add(String.format("Total: $%.2f", total));
        
        System.out.println("DEBUG: Mini cart updated. Total items in list: " + miniCartList.getItems().size());
    }

    private void loadProducts() {
        allProducts.clear();
        DatabaseAdapter db = DatabaseAdapter.getInstance();
        String query = "SELECT * FROM ProductInfo ORDER BY name";
        
        Connection conn = db.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Product p = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("type"),
                    rs.getDouble("price"),
                    rs.getDouble("stock"),
                    rs.getDouble("threshold"),
                    null
                );
                allProducts.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        renderProducts(allProducts);
    }

    private void renderProducts(List<Product> products) {
        vegPane.getChildren().clear();
        fruitPane.getChildren().clear();
        
        vegPane.setPrefWrapLength(700); // Helps with grid structure
        fruitPane.setPrefWrapLength(700);

        for (Product p : products) {
            VBox card = createProductCard(p);
            if ("Vegetable".equalsIgnoreCase(p.getType())) {
                vegPane.getChildren().add(card);
            } else {
                fruitPane.getChildren().add(card);
            }
        }
    }

    private VBox createProductCard(Product p) {
        VBox card = new VBox(10);
        card.getStyleClass().add("product-card");
        // Reduced width to fit 3 in a row
        card.setPrefWidth(180);
        card.setAlignment(Pos.CENTER);

        ImageView imgView = new ImageView(); 
        // Reduced image size as requested
        imgView.setFitHeight(80);
        imgView.setFitWidth(80);
        imgView.setPreserveRatio(true);
        
        java.io.InputStream is = DatabaseAdapter.getInstance().getProductImage(p.getId());
        if (is != null) {
            imgView.setImage(new javafx.scene.image.Image(is));
        }
        
        Label nameLbl = new Label(p.getName());
        nameLbl.getStyleClass().add("product-title");
        
        double displayPrice = p.getPrice();
        if (p.getStock() <= p.getThreshold()) {
             displayPrice *= 2.0;
        }
        
        Label priceLbl = new Label("$" + String.format("%.2f", displayPrice) + " / kg");
        priceLbl.getStyleClass().add("product-price");

        Label stockLbl = new Label("Stock: " + p.getStock() + " kg");
        stockLbl.getStyleClass().add("product-stock");

        // Quantity Controls
        HBox qtyBox = new HBox(5);
        qtyBox.setAlignment(Pos.CENTER);

        Button minusBtn = new Button("-");
        minusBtn.getStyleClass().add("qty-button");
        Label qtyLabel = new Label("1.0");
        qtyLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 30; -fx-alignment: center;");
        Button plusBtn = new Button("+");
        plusBtn.getStyleClass().add("qty-button");

        minusBtn.setOnAction(e -> {
            double val = Double.parseDouble(qtyLabel.getText());
            if (val > 0.25) qtyLabel.setText(String.format(Locale.US, "%.2f", val - 0.25));
        });

        plusBtn.setOnAction(e -> {
            double val = Double.parseDouble(qtyLabel.getText());
            if (val < p.getStock()) qtyLabel.setText(String.format(Locale.US, "%.2f", val + 0.25));
        });

        qtyBox.getChildren().addAll(minusBtn, qtyLabel, plusBtn);
        
        Button addBtn = new Button("Add to Cart");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        double finalPrice = displayPrice;
        
        if (p.getStock() <= 0) {
            addBtn.setDisable(true);
            addBtn.setText("Out of Stock");
            stockLbl.getStyleClass().add("product-stock-low");
            minusBtn.setDisable(true);
            plusBtn.setDisable(true);
        } else if (p.getStock() <= p.getThreshold()) {
            stockLbl.getStyleClass().add("product-stock-low");
            stockLbl.setText("Low Stock: " + p.getStock() + " kg");
        }
        
        addBtn.setOnAction(e -> {
            // Animation for visual feedback
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(100), addBtn);
            st.setByX(0.1);
            st.setByY(0.1);
            st.setCycleCount(2);
            st.setAutoReverse(true);
            st.play();

            addToCart(p, qtyLabel.getText(), finalPrice);
        });

        // Product Details on Image Click
        imgView.setOnMouseClicked(e -> showProductDetails(p));
        imgView.setCursor(javafx.scene.Cursor.HAND);

        card.getChildren().addAll(imgView, nameLbl, priceLbl, stockLbl, qtyBox, addBtn);
        return card;
    }

    private void showProductDetails(Product p) {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle(p.getName() + " Details");
        info.setHeaderText(null);

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);

        ImageView img = new ImageView();
        img.setFitWidth(150);
        img.setFitHeight(150);
        img.setPreserveRatio(true);
        java.io.InputStream is = DatabaseAdapter.getInstance().getProductImage(p.getId());
        if (is != null) img.setImage(new javafx.scene.image.Image(is));

        Label type = new Label("Category: " + p.getType());
        
        double displayPrice = p.getPrice();
        if (p.getStock() <= p.getThreshold()) {
            displayPrice *= 2.0;
        }
        
        Label price = new Label("Price: $" + String.format("%.2f", displayPrice) + " / kg");
        if (p.getStock() <= p.getThreshold()) {
            price.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
            price.setText("Price: $" + String.format("%.2f", displayPrice) + " / kg (⚠️ Threshold Price - Stock ≤ " + p.getThreshold() + " kg)");
        }
        
        Label stock = new Label("Available: " + p.getStock() + " kg");
        if (p.getStock() <= p.getThreshold()) {
            stock.setStyle("-fx-text-fill: #d32f2f;");
            stock.setText("⚠️ Low Stock: " + p.getStock() + " kg (Threshold: " + p.getThreshold() + " kg)");
        }
        Label desc = new Label("Fresh " + p.getName() + " sourced directly from local farms.");
        desc.setWrapText(true);

        content.getChildren().addAll(img, type, price, stock, new Separator(), desc);
        info.getDialogPane().setContent(content);
        info.showAndWait();
    }

    private void addToCart(Product p, String amountStr, double priceAtMoment) {
        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                showAlert("Invalid Amount", "Please enter a positive amount.");
                return;
            }
            
            // Check current stock
            if (amount > p.getStock()) {
                showAlert("Insufficient Stock", "Only " + p.getStock() + " kg available.");
                return;
            }
            
            // Check if product is already in cart and calculate total amount
            double alreadyInCart = CartController.getCartItems().stream()
                .filter(item -> item.getProduct().getId() == p.getId())
                .mapToDouble(CartController.CartItem::getAmount)
                .sum();
            
            double totalAmount = alreadyInCart + amount;
            
            if (totalAmount > p.getStock()) {
                showAlert("Insufficient Stock", 
                    String.format("You already have %.2f kg of %s in your cart.\n" +
                                "Only %.2f kg available in stock.\n" +
                                "You can add maximum %.2f kg more.", 
                                alreadyInCart, p.getName(), p.getStock(), p.getStock() - alreadyInCart));
                return;
            }
            
            CartController.addItem(p, amount, priceAtMoment);
            System.out.println("DEBUG: Added item to cart. Cart size: " + CartController.getCartItems().size());
            
            // Update product stock in memory (UI only, not database)
            p.setStock(p.getStock() - amount);
            
            // Refresh products display to show updated stock
            renderProducts(allProducts);
            
            // Update cart preview immediately
            updateMiniCart();
            showAlert("Success", "Added " + amount + "kg of " + p.getName() + " to cart.");
            
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number (e.g. 0.5).");
        }
    }

    @FXML
    private void handleSearch() {
        String term = searchField.getText().toLowerCase().trim();
        
        if (term.isEmpty()) {
            // If search is empty, show all products
            renderProducts(allProducts);
            // Expand both panes
            if (vegPaneTitled != null) vegPaneTitled.setExpanded(true);
            if (fruitPaneTitled != null) fruitPaneTitled.setExpanded(false);
            return;
        }
        
        List<Product> filtered = allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(term))
                .collect(Collectors.toList());
        renderProducts(filtered);
        
        // Auto-expand the appropriate category based on results
        if (!filtered.isEmpty()) {
            boolean hasVeg = filtered.stream().anyMatch(p -> "Vegetable".equalsIgnoreCase(p.getType()));
            boolean hasFruit = filtered.stream().anyMatch(p -> "Fruit".equalsIgnoreCase(p.getType()));
            
            if (vegPaneTitled != null) vegPaneTitled.setExpanded(hasVeg);
            if (fruitPaneTitled != null) fruitPaneTitled.setExpanded(hasFruit);
        }
    }
    
    @FXML
    private void openCart() {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cart.fxml"));
            Scene scene = new Scene(loader.load(), 600, 400);
            stage.setTitle("Shopping Cart");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void sendMessage() {
        String content = messageInput.getText().trim();
        if (content.isEmpty()) {
            showAlert("Empty Message", "Please write a message before sending.");
            return;
        }
        
        com.group27.model.User user = com.group27.utils.UserSession.getInstance().getCurrentUser();
        if (user == null) {
            showAlert("Error", "Please login to send messages.");
            return;
        }
        
        // Find owner ID from database
        String findOwnerQuery = "SELECT id FROM UserInfo WHERE role = 'owner' LIMIT 1";
        String insertQuery = "INSERT INTO Messages (sender_id, receiver_id, content) VALUES (?, ?, ?)";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        
        try {
            int ownerId = 0;
            try (PreparedStatement findStmt = conn.prepareStatement(findOwnerQuery)) {
                ResultSet rs = findStmt.executeQuery();
                if (rs.next()) {
                    ownerId = rs.getInt("id");
                }
            }
            
            if (ownerId == 0) {
                showAlert("Error", "Could not find owner to send message to.");
                return;
            }
            
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setInt(1, user.getId());
                stmt.setInt(2, ownerId);
                stmt.setString(3, content);
                stmt.executeUpdate();
                
                messageInput.clear();
                showAlert("Success!", "Your message has been sent to the owner.\nThey will reply soon!");
                
                // Reload messages to show the new one
                loadMessages();
                
                // Scroll to top to show new message
                if (messageList.getItems().size() > 0) {
                    messageList.scrollTo(0);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to send message: " + e.getMessage());
        }
    }
    
    private void loadMessages() {
        if (messageList == null) {
            System.out.println("DEBUG: messageList is NULL!");
            return;
        }
        messageList.getItems().clear();
        
        com.group27.model.User user = com.group27.utils.UserSession.getInstance().getCurrentUser();
        if (user == null) {
            messageList.getItems().add("Please login to view messages.");
            return;
        }
        
        // Get messages where user is EITHER sender OR receiver (for conversations with owner)
        String query = "SELECT * FROM Messages WHERE sender_id = ? OR receiver_id = ? ORDER BY timestamp DESC";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, user.getId());
            stmt.setInt(2, user.getId());
            ResultSet rs = stmt.executeQuery();
            
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("MMM dd, HH:mm");
            boolean hasMessages = false;
            
            while (rs.next()) {
                hasMessages = true;
                java.sql.Timestamp timestamp = rs.getTimestamp("timestamp");
                String formattedDate = timestamp != null ? dateFormat.format(timestamp) : "Unknown";
                int senderId = rs.getInt("sender_id");
                String content = rs.getString("content");
                String reply = rs.getString("reply");
                
                StringBuilder msgBuilder = new StringBuilder();
                msgBuilder.append("━━━━━━━━━━━━━━━━━━\n");
                
                // Check if this user sent the message or received it
                if (senderId == user.getId()) {
                    // User sent this message
                    msgBuilder.append("📤 You (").append(formattedDate).append("):\n");
                    msgBuilder.append("   ").append(content).append("\n");
                    
                    if (reply != null && !reply.trim().isEmpty()) {
                        msgBuilder.append("\n📥 Owner replied:\n");
                        msgBuilder.append("   ").append(reply).append("\n");
                    } else {
                        msgBuilder.append("\n⏳ Waiting for reply...\n");
                    }
                } else {
                    // User received this message (from owner)
                    msgBuilder.append("📥 Owner (").append(formattedDate).append("):\n");
                    msgBuilder.append("   ").append(content).append("\n");
                    
                    if (reply != null && !reply.trim().isEmpty()) {
                        msgBuilder.append("\n📤 You replied:\n");
                        msgBuilder.append("   ").append(reply).append("\n");
                    }
                }
                
                messageList.getItems().add(msgBuilder.toString());
            }
            
            System.out.println("DEBUG: Loaded " + messageList.getItems().size() + " messages");
            
            if (!hasMessages) {
                messageList.getItems().add("💬 No messages yet.\n\nSend a message to the owner using the box above!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("ERROR loading messages: " + e.getMessage());
            messageList.getItems().add("❌ Error loading messages: " + e.getMessage());
        }
    }

    @FXML
    private void openOrders() {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/orders.fxml"));
            Scene scene = new Scene(loader.load(), 500, 600);
            stage.setTitle("My Orders");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void rateCarrier(int orderId, int stars, int userId) {
        // Validate ownership and status
        String query = "UPDATE OrderInfo SET carrier_rating = ? WHERE id = ? AND user_id = ? AND isdelivered = TRUE";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, stars);
            stmt.setInt(2, orderId);
            stmt.setInt(3, userId);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                showAlert("Success", "Rating submitted!");
                // Ideally refresh the view, but closing/reopening works
            } else {
                showAlert("Error", "Cannot rate this order (Check ID, delivery status, or already rated).");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void cancelOrder(int orderId, int userId) {
        DatabaseAdapter db = DatabaseAdapter.getInstance();
        // Check eligibility: Not delivered, Time < 1 hour since order
        String checkQuery = "SELECT ordertime, isdelivered, products FROM OrderInfo WHERE id = ? AND user_id = ?";
        
        Connection conn = db.getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(checkQuery)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                if (rs.getBoolean("isdelivered")) {
                    showAlert("Error", "Cannot cancel delivered order.");
                    return;
                }
                java.sql.Timestamp ot = rs.getTimestamp("ordertime");
                if (ot != null) {
                    long diff = System.currentTimeMillis() - ot.getTime();
                    if (diff > 3600000) { // 1 hour in ms
                         showAlert("Error", "Cancellation period expired (1 hour).");
                         return;
                    }
                }
                
                // Restore Stock Logic (Crude Parsing of "Name:Amount;")
                String productStr = rs.getString("products");
                // Implementing stock restoration would require parsing this string and querying ProductInfo for IDs.
                // Given the text format limitation, this is complex.
                // We will delete the order to "Cancel" it.
                
                // Simple deletion for this assignment scope, or marking status.
                // Requirements say "Allow customers to cancel".
                
                String delQuery = "DELETE FROM OrderInfo WHERE id = ?";
                try (PreparedStatement delStmt = conn.prepareStatement(delQuery)) {
                    delStmt.setInt(1, orderId);
                    delStmt.executeUpdate();
                    showAlert("Success", "Order #" + orderId + " cancelled.");
                }
            } else {
                showAlert("Error", "Order not found.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML private ListView<String> miniCartList;
    @FXML private VBox profileBox;

    @FXML
    private void toggleProfile() {
        if (profileBox != null) {
            boolean visible = !profileBox.isVisible();
            profileBox.setVisible(visible);
            profileBox.setManaged(visible);
        }
    }

    @FXML
    private void updateProfile() {
        com.group27.model.User user = com.group27.utils.UserSession.getInstance().getCurrentUser();
        if (user == null) return;
        
        String newAddr = editAddressField.getText();
        String newPass = editPasswordField.getText();
        
        if (newAddr.isEmpty()) {
            showAlert("Error", "Address cannot be empty.");
            return;
        }
        
        // Update DB
        String query = "UPDATE UserInfo SET address = ?";
        if (!newPass.isEmpty()) {
            if (newPass.length() < 4) {
                showAlert("Error", "Password too short.");
                return;
            }
            query += ", password = ?";
        }
        query += " WHERE id = ?";
        
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            
            stmt.setString(1, newAddr);
            int paramIdx = 2;
            if (!newPass.isEmpty()) {
                stmt.setString(paramIdx++, newPass);
            }
            stmt.setInt(paramIdx, user.getId());
            
            stmt.executeUpdate();
            
            // Update Session
            user.setAddress(newAddr);
            showAlert("Success", "Profile Updated!");
            editPasswordField.clear();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Update failed.");
        }
    }

    @FXML
    private void handleLogout() {
        try {
            CartController.clearCart();
            com.group27.utils.UserSession.getInstance().clearSession();
            
            Stage stage = (Stage) searchField.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            stage.setScene(new Scene(loader.load(), 960, 540));
            stage.centerOnScreen();
        } catch (IOException e) {
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
    
    private void updateLoyaltyPoints() {
        com.group27.model.User user = com.group27.utils.UserSession.getInstance().getCurrentUser();
        if (user == null || loyaltyPointsLabel == null) return;
        
        // Refresh from database
        String query = "SELECT loyalty_points FROM UserInfo WHERE id = ?";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int points = rs.getInt("loyalty_points");
                user.setLoyaltyPoints(points);
                loyaltyPointsLabel.setText(String.valueOf(points));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void openMyCoupons() {
        com.group27.model.User user = com.group27.utils.UserSession.getInstance().getCurrentUser();
        if (user == null) return;
        
        // Show dialog with user's coupons
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("My Coupons");
        dialog.setHeaderText("Your Available Coupons");
        
        VBox content = new VBox(10);
        content.setStyle("-fx-padding: 10;");
        
        String query = "SELECT c.code, c.discount_amount, c.min_spend, uc.used " +
                      "FROM UserCoupons uc " +
                      "JOIN Coupons c ON uc.coupon_id = c.id " +
                      "WHERE uc.user_id = ? " +
                      "ORDER BY uc.used ASC, uc.acquired_date DESC";
        
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        boolean hasCoupons = false;
        
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                hasCoupons = true;
                String code = rs.getString("code");
                double discount = rs.getDouble("discount_amount");
                double minSpend = rs.getDouble("min_spend");
                boolean used = rs.getBoolean("used");
                
                HBox couponBox = new HBox(10);
                couponBox.setStyle("-fx-padding: 10; -fx-background-color: " + 
                    (used ? "#f5f5f5" : "#e8f5e9") + "; -fx-background-radius: 8;");
                
                VBox info = new VBox(3);
                Label codeLabel = new Label("🎟️ " + code);
                codeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                Label detailLabel = new Label(String.format("$%.2f off on orders $%.2f+", discount, minSpend));
                detailLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
                Label statusLabel = new Label(used ? "✓ Used" : "✓ Available");
                statusLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (used ? "#999" : "#4caf50") + ";");
                
                info.getChildren().addAll(codeLabel, detailLabel, statusLabel);
                couponBox.getChildren().add(info);
                content.getChildren().add(couponBox);
            }
            
            if (!hasCoupons) {
                Label noCoupons = new Label("You don't have any coupons yet.\nRedeem loyalty points to get coupons!");
                noCoupons.setStyle("-fx-text-fill: #666; -fx-font-size: 12px;");
                content.getChildren().add(noCoupons);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            content.getChildren().add(new Label("Error loading coupons"));
        }
        
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(300);
        dialog.getDialogPane().setContent(scroll);
        dialog.showAndWait();
    }
    
    @FXML
    private void openCouponShop() {
        com.group27.model.User user = com.group27.utils.UserSession.getInstance().getCurrentUser();
        if (user == null) return;
        
        // Show dialog to redeem points for coupons
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Coupon Shop");
        dialog.setHeaderText("Redeem Loyalty Points for Coupons");
        
        VBox content = new VBox(15);
        content.setStyle("-fx-padding: 15;");
        
        Label pointsLabel = new Label("Your Points: " + user.getLoyaltyPoints());
        pointsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ef6c00;");
        content.getChildren().add(pointsLabel);
        
        Separator sep = new Separator();
        content.getChildren().add(sep);
        
        Label infoLabel = new Label("Available Coupons to Redeem:");
        infoLabel.setStyle("-fx-font-weight: bold;");
        content.getChildren().add(infoLabel);
        
        // Load coupons from DB
        loadCouponOffers(content, user);
        
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(400);
        dialog.getDialogPane().setContent(scroll);
        
        dialog.getButtonTypes().clear();
        dialog.getButtonTypes().add(javafx.scene.control.ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    private void loadCouponOffers(VBox parent, com.group27.model.User user) {
        String query = "SELECT * FROM Coupons WHERE point_cost > 0 AND active = TRUE ORDER BY point_cost ASC";
        Connection conn = DatabaseAdapter.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            boolean hasCoupons = false;
            while(rs.next()) {
                hasCoupons = true;
                int id = rs.getInt("id");
                String code = rs.getString("code");
                double discount = rs.getDouble("discount_amount");
                double minSpend = rs.getDouble("min_spend");
                int pointCost = rs.getInt("point_cost");
                String desc = rs.getString("description");

                addCouponOffer(parent, user, id, code, discount, minSpend, pointCost, desc);
            }

            if (!hasCoupons) {
                parent.getChildren().add(new Label("No coupons available for redemption at the moment."));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            parent.getChildren().add(new Label("Error loading coupons."));
        }
    }

    private void addCouponOffer(VBox parent, com.group27.model.User user, int couponId, String code,
                                double discount, double minSpend, int pointCost, String description) {
        VBox offerBox = new VBox(8);
        offerBox.setStyle("-fx-padding: 12; -fx-background-color: #f5f5f5; -fx-background-radius: 8; -fx-border-color: #e0e0e0; -fx-border-radius: 8;");
        
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label codeLabel = new Label("🎟️ " + code);
        codeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label costLabel = new Label(pointCost + " points");
        costLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #ef6c00; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        header.getChildren().addAll(codeLabel, spacer, costLabel);
        
        Label detailLabel = new Label(String.format("$%.2f off on orders $%.2f+", discount, minSpend));
        detailLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");
        
        Label descLabel = new Label(description != null ? description : "");
        descLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #999; -fx-font-style: italic;");
        
        Button redeemBtn = new Button("Redeem");
        redeemBtn.setMaxWidth(Double.MAX_VALUE);
        
        // Check if user already has this coupon (unused)
        boolean alreadyHas = checkUserHasCoupon(user.getId(), couponId);

        if (alreadyHas) {
            redeemBtn.setDisable(true);
            redeemBtn.setText("Already Redeemed");
            redeemBtn.setStyle("-fx-text-fill: #4caf50;");
        } else if (user.getLoyaltyPoints() < pointCost) {
            redeemBtn.setDisable(true);
            redeemBtn.setText("Not enough points");
            redeemBtn.setStyle("-fx-text-fill: #999;");
        }
        
        redeemBtn.setOnAction(e -> {
            redeemCoupon(user, couponId, code, pointCost);
            ((Stage) redeemBtn.getScene().getWindow()).close();
        });
        
        offerBox.getChildren().addAll(header, detailLabel, descLabel, redeemBtn);
        parent.getChildren().add(offerBox);
    }
    
    private boolean checkUserHasCoupon(int userId, int couponId) {
        String query = "SELECT id FROM UserCoupons WHERE user_id = ? AND coupon_id = ? AND used = FALSE";
        try (PreparedStatement stmt = DatabaseAdapter.getInstance().getConnection().prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, couponId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void redeemCoupon(com.group27.model.User user, int couponId, String code, int pointCost) {
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        
        try {
            conn.setAutoCommit(false);
            
            // Add coupon to user
            String addUserCoupon = "INSERT INTO UserCoupons (user_id, coupon_id) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(addUserCoupon)) {
                stmt.setInt(1, user.getId());
                stmt.setInt(2, couponId);
                stmt.executeUpdate();
            }
            
            // Deduct points
            String updatePoints = "UPDATE UserInfo SET loyalty_points = loyalty_points - ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updatePoints)) {
                stmt.setInt(1, pointCost);
                stmt.setInt(2, user.getId());
                stmt.executeUpdate();
            }
            
            conn.commit();
            
            // Update local user object
            user.setLoyaltyPoints(user.getLoyaltyPoints() - pointCost);
            updateLoyaltyPoints();
            
            showAlert("Success!", "Coupon '" + code + "' has been added to your account!\nYou can use it at checkout.");
            
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            showAlert("Error", "Failed to redeem coupon: " + e.getMessage());
        } finally {
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
