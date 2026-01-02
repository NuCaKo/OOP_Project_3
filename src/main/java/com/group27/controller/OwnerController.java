package com.group27.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import com.group27.core.DatabaseAdapter;
import com.group27.model.Order;
import com.group27.model.Product;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class OwnerController {

    // Dashboard
    @FXML private Label lowStockAlert;
    @FXML private Label totalRevenueLabel;
    @FXML private Label totalOrdersLabel;
    @FXML private Label stockValueLabel;
    @FXML private ListView<String> lowStockList;
    @FXML private javafx.scene.chart.BarChart<String, Number> salesChart;
    
    // Reports Charts
    @FXML private javafx.scene.chart.BarChart<String, Number> productSalesChart;
    @FXML private javafx.scene.chart.LineChart<String, Number> timeSalesChart;
    @FXML private javafx.scene.chart.PieChart revenueChart;
    
    // Products
    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Number> idCol;
    @FXML private TableColumn<Product, String> nameCol;
    @FXML private TableColumn<Product, String> typeCol;
    @FXML private TableColumn<Product, Number> priceCol;
    @FXML private TableColumn<Product, Number> stockCol;
    @FXML private TableColumn<Product, Number> thresholdCol;
    @FXML private TableColumn<Product, String> statusCol;
    @FXML private TextField productSearchField;
    @FXML private ComboBox<String> filterTypeBox;
    @FXML private Label productCountLabel;

    @FXML private TextField pName;
    @FXML private ComboBox<String> pType;
    @FXML private TextField pPrice;
    @FXML private TextField pStock;
    @FXML private TextField pThreshold;

    // Orders
    @FXML private TableView<Order> orderTable;
    @FXML private TableColumn<Order, Number> orderIdCol;
    @FXML private TableColumn<Order, String> orderDateCol;
    @FXML private TableColumn<Order, String> orderProductsCol;
    @FXML private TableColumn<Order, Number> orderTotalCol;
    @FXML private TableColumn<Order, Number> carrierCol;
    @FXML private TableColumn<Order, String> orderStatusCol;
    @FXML private ComboBox<String> orderStatusFilter;
    @FXML private Label orderCountLabel;
    @FXML private Label pendingOrdersLabel;
    @FXML private Label deliveredOrdersLabel;
    
    // Messages
    @FXML private ListView<com.group27.model.Message> messageList;
    @FXML private TextArea replyInput;
    @FXML private Label unreadMessagesLabel;
    @FXML private Label selectedMessageLabel;
    
    // Carriers
    @FXML private ListView<String> carrierList;
    @FXML private TextField carrierUsername;
    @FXML private javafx.scene.control.PasswordField carrierPassword;
    
    // Coupons
    @FXML private ListView<String> couponList;
    @FXML private TextField couponCode;
    @FXML private TextField couponDiscount;
    @FXML private TextField couponMinSpend;

    private ObservableList<Product> productList = FXCollections.observableArrayList();
    private ObservableList<Product> filteredProductList = FXCollections.observableArrayList();
    private ObservableList<Order> orderList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        pType.getItems().addAll("Vegetable", "Fruit");
        
        if (filterTypeBox != null) {
            filterTypeBox.getItems().addAll("All Types", "Vegetable", "Fruit");
            filterTypeBox.setValue("All Types");
        }
        
        if (orderStatusFilter != null) {
            orderStatusFilter.getItems().addAll("All Orders", "Pending", "Delivered");
            orderStatusFilter.setValue("All Orders");
        }
        
        setupProductTable();
        setupOrderTable();
        
        loadProducts();
        loadOrders();
        updateDashboard();
        loadMessages();
        loadCarriers();
        loadCoupons();
        updateReports();
        
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                pName.setText(newVal.getName());
                pType.setValue(newVal.getType());
                pPrice.setText(String.valueOf(newVal.getPrice()));
                pStock.setText(String.valueOf(newVal.getStock()));
                pThreshold.setText(String.valueOf(newVal.getThreshold()));
            }
        });
        
        messageList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && selectedMessageLabel != null) {
                selectedMessageLabel.setText("From User ID " + newVal.getSenderId() + ": " + newVal.getContent());
            }
        });
    }

    private void updateDashboard() {
        double revenue = orderList.stream().mapToDouble(Order::getTotalCost).sum();
        int count = orderList.size();
        double stockValue = productList.stream().mapToDouble(p -> p.getPrice() * p.getStock()).sum();
        
        if (totalRevenueLabel != null)
            totalRevenueLabel.setText("$" + String.format("%.2f", revenue));
        if (totalOrdersLabel != null)
            totalOrdersLabel.setText(String.valueOf(count));
        if (stockValueLabel != null)
            stockValueLabel.setText("$" + String.format("%.2f", stockValue));
            
        // Low Stock Alerts
        updateLowStockAlerts();
        
        // Update order statistics
        updateOrderStatistics();
        
        // Populate Chart
        if (salesChart != null) {
            salesChart.getData().clear();
            javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
            series.setName("Financial Overview");
            
            series.getData().add(new javafx.scene.chart.XYChart.Data<>("Total Revenue", revenue));
            series.getData().add(new javafx.scene.chart.XYChart.Data<>("Stock Value", stockValue));
            
            // Calculate pending orders value
            double pendingValue = orderList.stream()
                .filter(o -> !o.isDelivered())
                .mapToDouble(Order::getTotalCost)
                .sum();
            series.getData().add(new javafx.scene.chart.XYChart.Data<>("Pending Orders", pendingValue));
            
            salesChart.getData().add(series);
        }
    }
    
    private void updateLowStockAlerts() {
        if (lowStockList != null) {
            lowStockList.getItems().clear();
            int lowStockCount = 0;
            
            for (Product p : productList) {
                if (p.getStock() <= p.getThreshold()) {
                    lowStockList.getItems().add(
                        String.format("⚠️ %s - Stock: %.1fkg (Threshold: %.1fkg)", 
                            p.getName(), p.getStock(), p.getThreshold())
                    );
                    lowStockCount++;
                }
            }
            
            if (lowStockCount == 0) {
                lowStockList.getItems().add("✅ All products are well stocked!");
            }
            
            if (lowStockAlert != null) {
                if (lowStockCount > 0) {
                    lowStockAlert.setText("⚠️ " + lowStockCount + " low stock items");
                } else {
                    lowStockAlert.setText("");
                }
            }
        }
    }
    
    private void updateOrderStatistics() {
        long pending = orderList.stream().filter(o -> !o.isDelivered()).count();
        long delivered = orderList.stream().filter(Order::isDelivered).count();
        
        if (orderCountLabel != null)
            orderCountLabel.setText(String.valueOf(orderList.size()));
        if (pendingOrdersLabel != null)
            pendingOrdersLabel.setText(String.valueOf(pending));
        if (deliveredOrdersLabel != null)
            deliveredOrdersLabel.setText(String.valueOf(delivered));
    }

    private void loadMessages() {
        if (messageList == null) return;
        messageList.getItems().clear();
        int unreadCount = 0;
        
        String query = "SELECT * FROM Messages ORDER BY timestamp DESC";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                LocalDateTime ts = rs.getTimestamp("timestamp").toLocalDateTime();
                String reply = rs.getString("reply");
                
                if (reply == null || reply.isEmpty()) {
                    unreadCount++;
                }
                
                messageList.getItems().add(new com.group27.model.Message(
                    rs.getInt("id"),
                    rs.getInt("sender_id"),
                    rs.getInt("receiver_id"),
                    rs.getString("content"),
                    ts,
                    reply
                ));
            }
            
            if (unreadMessagesLabel != null) {
                unreadMessagesLabel.setText("Unread: " + unreadCount);
            }
            
            messageList.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(com.group27.model.Message item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        String txt = "From User ID " + item.getSenderId() + ": " + item.getContent();
                        if (item.getReply() != null && !item.getReply().isEmpty()) {
                            txt += "\n✅ Replied: " + item.getReply();
                            setStyle("-fx-text-fill: #666;");
                        } else {
                            txt = "📩 " + txt;
                            setStyle("-fx-font-weight: bold;");
                        }
                        setText(txt);
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void sendReply() {
        com.group27.model.Message msg = messageList.getSelectionModel().getSelectedItem();
        if (msg == null || replyInput.getText().isEmpty()) return;
        
        String query = "UPDATE Messages SET reply = ? WHERE id = ?";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, replyInput.getText());
            stmt.setInt(2, msg.getId());
            stmt.executeUpdate();
            replyInput.clear();
            loadMessages();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void loadCarriers() {
        if (carrierList == null) return;
        carrierList.getItems().clear();
        // Calculate average rating for each carrier
        // Need to join UserInfo with OrderInfo where carrier_id = user.id
        // Complex query, let's do simple iterate or join.
        // SELECT u.username, AVG(o.carrier_rating) FROM UserInfo u LEFT JOIN OrderInfo o ON u.id = o.carrier_id WHERE u.role='carrier' GROUP BY u.id
        
        String query = "SELECT u.id, u.username, AVG(NULLIF(o.carrier_rating, 0)) as avg_rating " +
                       "FROM UserInfo u " +
                       "LEFT JOIN OrderInfo o ON u.id = o.carrier_id " +
                       "WHERE u.role = 'carrier' " +
                       "GROUP BY u.id, u.username";
                       
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String name = rs.getString("username");
                double rating = rs.getDouble("avg_rating"); // returns 0 if null usually or check null
                if (rs.wasNull()) rating = 0.0;
                
                carrierList.getItems().add(name + " (Rating: " + String.format("%.1f", rating) + "/5)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void addCarrier() {
        if (carrierUsername.getText().isEmpty() || carrierPassword.getText().isEmpty()) return;
        if (DatabaseAdapter.getInstance().registerUser(carrierUsername.getText(), carrierPassword.getText(), "carrier", "Station")) {
            loadCarriers();
            carrierUsername.clear();
            carrierPassword.clear();
        } else {
            showAlert("Error", "Could not add carrier");
        }
    }
    
    @FXML
    private void removeCarrier() {
        String selected = carrierList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("No Selection", "Please select a carrier to remove.");
            return;
        }
        
        // Parse "username (Rating..."
        String username = selected.split(" \\(")[0];
        
        // Get carrier ID
        String getCarrierIdQuery = "SELECT id FROM UserInfo WHERE username = ? AND role = 'carrier'";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        int carrierId = 0;
        
        try (PreparedStatement stmt = conn.prepareStatement(getCarrierIdQuery)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                carrierId = rs.getInt("id");
            } else {
                showAlert("Error", "Carrier not found.");
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to check carrier status.");
            return;
        }
        
        // Check if carrier has pending (undelivered) orders
        String checkPendingOrdersQuery = "SELECT COUNT(*) as pending_count FROM OrderInfo WHERE carrier_id = ? AND isdelivered = FALSE";
        try (PreparedStatement stmt = conn.prepareStatement(checkPendingOrdersQuery)) {
            stmt.setInt(1, carrierId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int pendingCount = rs.getInt("pending_count");
                if (pendingCount > 0) {
                    showAlert("Cannot Remove Carrier", 
                        String.format("Cannot remove carrier '%s' because they have %d pending order(s).\n" +
                                    "Please wait until all orders are delivered.", username, pendingCount));
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to check pending orders.");
            return;
        }
        
        // Safe to remove - no pending orders
        String deleteQuery = "DELETE FROM UserInfo WHERE id = ? AND role = 'carrier'";
        try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.setInt(1, carrierId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                showAlert("Success", "Carrier '" + username + "' has been removed successfully.");
                loadCarriers();
            } else {
                showAlert("Error", "Failed to remove carrier.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to remove carrier: " + e.getMessage());
        }
    }
    
    private void loadCoupons() {
        if (couponList == null) return;
        couponList.getItems().clear();
        String query = "SELECT * FROM Coupons";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                String c = rs.getString("code") + " - $" + rs.getDouble("discount_amount") + " off (Min $" + rs.getDouble("min_spend") + ")";
                couponList.getItems().add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void addCoupon() {
        try {
            String code = couponCode.getText();
            double discount = Double.parseDouble(couponDiscount.getText());
            double minSpend = Double.parseDouble(couponMinSpend.getText());
            
            String query = "INSERT INTO Coupons (code, discount_amount, min_spend) VALUES (?, ?, ?)";
            Connection conn = DatabaseAdapter.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, code);
                stmt.setDouble(2, discount);
                stmt.setDouble(3, minSpend);
                stmt.executeUpdate();
                loadCoupons();
                couponCode.clear();
                couponDiscount.clear();
                couponMinSpend.clear();
            }
        } catch (Exception e) {
            showAlert("Error", "Invalid Input");
        }
    }
    
    @FXML
    private void removeCoupon() {
        String selected = couponList.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        // Parse code "CODE - ..."
        String code = selected.split(" - ")[0];
        String query = "DELETE FROM Coupons WHERE code = ?";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, code);
            stmt.executeUpdate();
            loadCoupons();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupProductTable() {
        idCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        nameCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getName()));
        typeCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getType()));
        priceCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPrice()));
        stockCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getStock()));
        thresholdCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getThreshold()));
        
        if (statusCol != null) {
            statusCol.setCellValueFactory(d -> {
                Product p = d.getValue();
                String status = p.getStock() <= p.getThreshold() ? "⚠️ Low Stock" : "✅ OK";
                return new SimpleStringProperty(status);
            });
        }
        
        productTable.setItems(filteredProductList);
        updateProductCount();
    }
    
    private void setupOrderTable() {
        orderIdCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        orderDateCol.setCellValueFactory(d -> {
            LocalDateTime dt = d.getValue().getOrderTime();
            return new SimpleObjectProperty<>(dt != null ? dt.toString().replace("T", " ") : "N/A");
        });
        
        if (orderProductsCol != null) {
            orderProductsCol.setCellValueFactory(d -> {
                String products = d.getValue().getProductsJson();
                // Shorten for display: "Potato:2.0;Apple:1.0;" -> "Potato, Apple, ..."
                if (products != null && !products.isEmpty()) {
                    String[] items = products.split(";");
                    String display = java.util.Arrays.stream(items)
                        .limit(3)
                        .map(item -> item.split(":")[0])
                        .collect(java.util.stream.Collectors.joining(", "));
                    if (items.length > 3) display += "...";
                    return new SimpleStringProperty(display);
                }
                return new SimpleStringProperty("N/A");
            });
        }
        
        orderTotalCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTotalCost()));
        carrierCol.setCellValueFactory(d -> {
            int cid = d.getValue().getCarrierId();
            return new SimpleIntegerProperty(cid > 0 ? cid : 0);
        });
        
        if (orderStatusCol != null) {
            orderStatusCol.setCellValueFactory(d -> {
                String status = d.getValue().isDelivered() ? "✅ Delivered" : "⏳ Pending";
                return new SimpleStringProperty(status);
            });
        }
        
        orderTable.setItems(orderList);
    }

    private void loadProducts() {
        productList.clear();
        String query = "SELECT * FROM ProductInfo ORDER BY name";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                productList.add(new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getDouble("price"),
                        rs.getDouble("stock"),
                        rs.getDouble("threshold"),
                        null
                ));
            }
            filterProducts();
            updateProductCount();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void filterProducts() {
        filteredProductList.clear();
        
        String searchText = productSearchField != null ? productSearchField.getText().toLowerCase() : "";
        String typeFilter = filterTypeBox != null ? filterTypeBox.getValue() : "All Types";
        
        for (Product p : productList) {
            boolean matchesSearch = searchText.isEmpty() || 
                p.getName().toLowerCase().contains(searchText);
            boolean matchesType = typeFilter == null || typeFilter.equals("All Types") || 
                p.getType().equals(typeFilter);
                
            if (matchesSearch && matchesType) {
                filteredProductList.add(p);
            }
        }
        
        updateProductCount();
    }
    
    private void updateProductCount() {
        if (productCountLabel != null) {
            productCountLabel.setText("Total: " + filteredProductList.size() + " products");
        }
    }
    
    @FXML
    private void handleProductSearch() {
        filterProducts();
    }
    
    @FXML
    private void handleTypeFilter() {
        filterProducts();
    }
    
    @FXML
    private void handleOrderFilter() {
        // Implementation for order filtering can be added
        updateOrderStatistics();
    }
    
    private void updateReports() {
        // Product-based Sales Chart
        if (productSalesChart != null) {
            productSalesChart.getData().clear();
            java.util.Map<String, Double> productSales = new java.util.HashMap<>();
            
            // Parse products from orders and aggregate by product name
            for (Order order : orderList) {
                String productsJson = order.getProductsJson();
                if (productsJson != null && !productsJson.isEmpty()) {
                    String[] items = productsJson.split(";");
                    for (String item : items) {
                        item = item.trim();
                        if (item.isEmpty()) continue;
                        try {
                            String[] parts = item.split(":");
                            if (parts.length == 2) {
                                String productName = parts[0].trim();
                                double amount = Double.parseDouble(parts[1].trim());
                                productSales.put(productName, productSales.getOrDefault(productName, 0.0) + amount);
                            }
                        } catch (Exception e) {
                            // Skip invalid entries
                        }
                    }
                }
            }
            
            javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
            series.setName("Quantity Sold (kg)");
            for (java.util.Map.Entry<String, Double> entry : productSales.entrySet()) {
                series.getData().add(new javafx.scene.chart.XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            productSalesChart.getData().add(series);
        }
        
        // Time-based Sales Chart
        if (timeSalesChart != null) {
            timeSalesChart.getData().clear();
            java.util.Map<String, Double> dailyRevenue = new java.util.HashMap<>();
            
            // Group orders by date
            for (Order order : orderList) {
                if (order.getOrderTime() != null) {
                    String dateKey = order.getOrderTime().format(java.time.format.DateTimeFormatter.ofPattern("MMM dd"));
                    dailyRevenue.put(dateKey, dailyRevenue.getOrDefault(dateKey, 0.0) + order.getTotalCost());
                }
            }
            
            javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
            series.setName("Revenue ($)");
            // Sort by date for better visualization
            java.util.List<String> sortedDates = new java.util.ArrayList<>(dailyRevenue.keySet());
            java.util.Collections.sort(sortedDates);
            for (String date : sortedDates) {
                series.getData().add(new javafx.scene.chart.XYChart.Data<>(date, dailyRevenue.get(date)));
            }
            timeSalesChart.getData().add(series);
        }
        
        // Money-based Revenue Chart (Pie Chart)
        if (revenueChart != null) {
            revenueChart.getData().clear();
            
            double totalRevenue = orderList.stream().mapToDouble(Order::getTotalCost).sum();
            
            if (totalRevenue > 0) {
                double deliveredRevenue = orderList.stream()
                    .filter(Order::isDelivered)
                    .mapToDouble(Order::getTotalCost)
                    .sum();
                double pendingRevenue = totalRevenue - deliveredRevenue;
                
                javafx.scene.chart.PieChart.Data deliveredData = new javafx.scene.chart.PieChart.Data(
                    String.format("Delivered (%.1f%%)", (deliveredRevenue / totalRevenue * 100)), deliveredRevenue);
                javafx.scene.chart.PieChart.Data pendingData = new javafx.scene.chart.PieChart.Data(
                    String.format("Pending (%.1f%%)", (pendingRevenue / totalRevenue * 100)), pendingRevenue);
                
                revenueChart.getData().addAll(deliveredData, pendingData);
            }
        }
    }
    
    @FXML
    private void refreshAll() {
        loadProducts();
        loadOrders();
        updateDashboard();
        updateReports();
        loadMessages();
        loadCarriers();
        loadCoupons();
        showAlert("Refreshed", "All data has been refreshed successfully!");
    }
    
    private void loadOrders() {
        orderList.clear();
        String query = "SELECT * FROM OrderInfo";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                LocalDateTime ot = rs.getTimestamp("ordertime") != null ? rs.getTimestamp("ordertime").toLocalDateTime() : null;
                LocalDateTime dt = rs.getTimestamp("deliverytime") != null ? rs.getTimestamp("deliverytime").toLocalDateTime() : null;
                
                orderList.add(new Order(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("carrier_id"),
                        ot,
                        dt,
                        rs.getString("products"),
                        rs.getDouble("totalcost"),
                        rs.getBoolean("isdelivered")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void addProduct() {
        try {
            String name = pName.getText();
            String type = pType.getValue();
            double price = Double.parseDouble(pPrice.getText());
            double stock = Double.parseDouble(pStock.getText());
            double threshold = Double.parseDouble(pThreshold.getText());
            
            if (stock < 0 || threshold < 0 || price <= 0) {
                 showAlert("Error", "Invalid values. Price must be > 0. Stock/Threshold >= 0.");
                 return;
            }

            String query = "INSERT INTO ProductInfo (name, type, price, stock, threshold) VALUES (?, ?, ?, ?, ?)";
            Connection conn = DatabaseAdapter.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, name);
                stmt.setString(2, type);
                stmt.setDouble(3, price);
                stmt.setDouble(4, stock);
                stmt.setDouble(5, threshold);
                stmt.executeUpdate();
                loadProducts();
                updateDashboard();
                clearForm();
                showAlert("Success", "Product added successfully!");
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid number format");
        } catch (SQLException e) {
            showAlert("Error", "Failed to add product: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void updateProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        try {
            double price = Double.parseDouble(pPrice.getText());
            double stock = Double.parseDouble(pStock.getText());
            double threshold = Double.parseDouble(pThreshold.getText());
            
             if (stock < 0 || threshold < 0 || price <= 0) {
                 showAlert("Error", "Invalid values. Price must be > 0. Stock/Threshold >= 0.");
                 return;
            }
            
            String query = "UPDATE ProductInfo SET name=?, type=?, price=?, stock=?, threshold=? WHERE id=?";
            Connection conn = DatabaseAdapter.getInstance().getConnection();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, pName.getText());
                stmt.setString(2, pType.getValue());
                stmt.setDouble(3, price);
                stmt.setDouble(4, stock);
                stmt.setDouble(5, threshold);
                stmt.setInt(6, selected.getId());
                stmt.executeUpdate();
                loadProducts();
                updateDashboard();
                clearForm();
                showAlert("Success", "Product updated successfully!");
            }
        } catch (Exception e) {
            showAlert("Error", "Update failed: " + e.getMessage());
        }
    }
    
    @FXML
    private void deleteProduct() {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        try {
             String query = "DELETE FROM ProductInfo WHERE id=?";
             Connection conn = DatabaseAdapter.getInstance().getConnection();
             try (PreparedStatement stmt = conn.prepareStatement(query)) {
                 stmt.setInt(1, selected.getId());
                 stmt.executeUpdate();
                 loadProducts();
                 updateDashboard();
                 clearForm();
                 showAlert("Success", "Product deleted successfully!");
             }
        } catch (Exception e) {
             showAlert("Error", "Delete failed: " + e.getMessage());
        }
    }
    
    @FXML
    private void clearForm() {
        pName.clear();
        pPrice.clear();
        pStock.clear();
        pThreshold.clear();
        productTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleLogout() {
        try {
            com.group27.utils.UserSession.getInstance().clearSession();
            Stage stage = (Stage) productTable.getScene().getWindow();
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
        alert.setContentText(content);
        alert.showAndWait();
    }
}
