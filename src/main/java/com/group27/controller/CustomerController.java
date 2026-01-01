package com.group27.controller;

import com.group27.core.DatabaseAdapter;
import com.group27.model.Product;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    private List<Product> allProducts = new ArrayList<>();
    
    @FXML
    public void initialize() {
        com.group27.model.User user = com.group27.utils.UserSession.getInstance().getCurrentUser();
        if (user != null) {
            welcomeLabel.setText("Welcome, " + user.getUsername());
            if (editAddressField != null) editAddressField.setText(user.getAddress());
        } else {
            welcomeLabel.setText("Welcome, Valued Customer");
        }
        
        loadProducts();
        loadMessages();
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
        
        for (Product p : products) {
            // Option: Show zero stock items but disable adding them.
            
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
        card.setPrefWidth(200);
        card.setAlignment(Pos.CENTER);

        ImageView imgView = new ImageView(); 
        imgView.setFitHeight(120);
        imgView.setFitWidth(120);
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
            if (val > 0.25) qtyLabel.setText(String.format("%.2f", val - 0.25));
        });

        plusBtn.setOnAction(e -> {
            double val = Double.parseDouble(qtyLabel.getText());
            if (val < p.getStock()) qtyLabel.setText(String.format("%.2f", val + 0.25));
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
        Label price = new Label("Price: $" + p.getPrice() + " / kg");
        Label stock = new Label("Available: " + p.getStock() + " kg");
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
            if (amount > p.getStock()) {
                showAlert("Insufficient Stock", "Only " + p.getStock() + " kg available.");
                return;
            }
            
            CartController.addItem(p, amount, priceAtMoment);
            showAlert("Success", "Added " + amount + "kg of " + p.getName() + " to cart.");
            
        } catch (NumberFormatException e) {
            showAlert("Invalid Input", "Please enter a valid number (e.g. 0.5).");
        }
    }

    @FXML
    private void handleSearch() {
        String term = searchField.getText().toLowerCase();
        List<Product> filtered = allProducts.stream()
                .filter(p -> p.getName().toLowerCase().contains(term))
                .collect(Collectors.toList());
        renderProducts(filtered);
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
        String content = messageInput.getText();
        if (content.isEmpty()) return;
        
        com.group27.model.User user = com.group27.utils.UserSession.getInstance().getCurrentUser();
        if (user == null) return;
        
        int ownerId = 3; 
        
        String query = "INSERT INTO Messages (sender_id, receiver_id, content) VALUES (?, ?, ?)";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, user.getId());
            stmt.setInt(2, ownerId);
            stmt.setString(3, content);
            stmt.executeUpdate();
            
            messageInput.clear();
            loadMessages();
            showAlert("Success", "Message sent to owner.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadMessages() {
        if (messageList == null) return;
        messageList.getItems().clear();
        
        com.group27.model.User user = com.group27.utils.UserSession.getInstance().getCurrentUser();
        if (user == null) return;
        
        String query = "SELECT * FROM Messages WHERE sender_id = ? ORDER BY timestamp DESC";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String msg = "Me: " + rs.getString("content");
                String reply = rs.getString("reply");
                if (reply != null && !reply.isEmpty()) {
                    msg += "\nOwner: " + reply;
                }
                msg += "\n(" + rs.getTimestamp("timestamp") + ")";
                messageList.getItems().add(msg);
            }
        } catch (SQLException e) {
            e.printStackTrace();
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
}
