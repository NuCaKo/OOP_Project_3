package com.group27.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import com.group27.core.DatabaseAdapter;
import com.group27.model.Order;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controller for the Carrier interface.
 * Handles order management for carriers.
 */
public class CarrierController {

    @FXML private Label carrierNameLabel;
    
    @FXML private TableView<Order> availableTable;
    @FXML private TableColumn<Order, Number> avIdCol;
    @FXML private TableColumn<Order, String> avCustomerCol;
    @FXML private TableColumn<Order, String> avAddressCol;
    @FXML private TableColumn<Order, String> avProductsCol;
    @FXML private TableColumn<Order, String> avDateCol;
    @FXML private TableColumn<Order, Number> avTotalCol;

    @FXML private TableView<Order> myTable;
    @FXML private TableColumn<Order, Number> myIdCol;
    @FXML private TableColumn<Order, String> myCustomerCol;
    @FXML private TableColumn<Order, String> myAddressCol;
    @FXML private TableColumn<Order, String> myDateCol;
    @FXML private TableColumn<Order, Number> myTotalCol;

    @FXML private TableView<Order> completedTable;
    @FXML private TableColumn<Order, Number> cpIdCol;
    @FXML private TableColumn<Order, String> cpCustomerCol;
    @FXML private TableColumn<Order, String> cpDateCol;
    @FXML private TableColumn<Order, Number> cpTotalCol;

    private ObservableList<Order> availableList = FXCollections.observableArrayList();
    private ObservableList<Order> myList = FXCollections.observableArrayList();
    private ObservableList<Order> completedList = FXCollections.observableArrayList();
    
    private int carrierId;

    /**
     * Initializes the controller class.
     */
    @FXML
    public void initialize() {
        com.group27.model.User user = com.group27.utils.UserSession.getInstance().getCurrentUser();
        this.carrierId = (user != null) ? user.getId() : 0;
        
        if (user != null && carrierNameLabel != null) {
            carrierNameLabel.setText("👤 " + user.getUsername());
        }
        
        setupTables();
        refreshTables();
    }
    
    /**
     * Sets up the table columns and data sources.
     */
    private void setupTables() {
        // Available Orders - Enable multiple selection
        availableTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        avIdCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        avCustomerCol.setCellValueFactory(d -> new SimpleStringProperty(getCustomerName(d.getValue().getUserId())));
        avAddressCol.setCellValueFactory(d -> new SimpleStringProperty(getCustomerAddress(d.getValue().getUserId())));
        avProductsCol.setCellValueFactory(d -> new SimpleStringProperty(getProductSummary(d.getValue().getProductsJson())));
        avDateCol.setCellValueFactory(d -> {
            LocalDateTime dt = d.getValue().getDeliveryTime();
            return new SimpleStringProperty(dt != null ? dt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "");
        });
        avTotalCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTotalCost()));
        availableTable.setItems(availableList);
        
        // My Orders - Enable multiple selection for batch completion
        myTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        myIdCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        myCustomerCol.setCellValueFactory(d -> new SimpleStringProperty(getCustomerName(d.getValue().getUserId())));
        myAddressCol.setCellValueFactory(d -> new SimpleStringProperty(getCustomerAddress(d.getValue().getUserId())));
        myDateCol.setCellValueFactory(d -> {
            LocalDateTime dt = d.getValue().getDeliveryTime();
            return new SimpleStringProperty(dt != null ? dt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "");
        });
        myTotalCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTotalCost()));
        myTable.setItems(myList);
        
        // Completed Orders
        cpIdCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        cpCustomerCol.setCellValueFactory(d -> new SimpleStringProperty(getCustomerName(d.getValue().getUserId())));
        cpDateCol.setCellValueFactory(d -> {
            LocalDateTime dt = d.getValue().getDeliveryTime();
            return new SimpleStringProperty(dt != null ? dt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "");
        });
        cpTotalCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTotalCost()));
        completedTable.setItems(completedList);
    }
    
    /**
     * Retrieves customer username by ID.
     */
    private String getCustomerName(int userId) {
        String query = "SELECT username FROM UserInfo WHERE id = ?";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Unknown";
    }
    
    /**
     * Retrieves customer address by ID.
     */
    private String getCustomerAddress(int userId) {
        String query = "SELECT address FROM UserInfo WHERE id = ?";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String addr = rs.getString("address");
                return (addr != null && !addr.isEmpty()) ? addr : "No address";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "No address";
    }
    
    /**
     * Generates a summary string of products in an order.
     */
    private String getProductSummary(String productsString) {
        if (productsString == null || productsString.isEmpty()) return "N/A";
        
        String[] items = productsString.split(";");
        int count = 0;
        for (String item : items) {
            if (item.trim().length() > 0 && item.contains(":")) {
                count++;
            }
        }
        return count + " item(s)";
    }
    
    /**
     * Refreshes the order tables.
     */
    @FXML
    private void refreshTables() {
        availableList.clear();
        myList.clear();
        completedList.clear();
        
        String query = "SELECT * FROM OrderInfo ORDER BY ordertime DESC";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                LocalDateTime ot = rs.getTimestamp("ordertime") != null ? rs.getTimestamp("ordertime").toLocalDateTime() : null;
                LocalDateTime dt = rs.getTimestamp("deliverytime") != null ? rs.getTimestamp("deliverytime").toLocalDateTime() : null;
                
                Order o = new Order(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("carrier_id"),
                        ot,
                        dt,
                        rs.getString("products"),
                        rs.getDouble("totalcost"),
                        rs.getBoolean("isdelivered")
                );
                
                if (o.isDelivered() && o.getCarrierId() == carrierId) {
                    completedList.add(o);
                } else if (o.getCarrierId() == carrierId && !o.isDelivered()) {
                    myList.add(o);
                } else if (o.getCarrierId() == 0 && !o.isDelivered()) {
                    availableList.add(o);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Assigns selected available orders to the current carrier.
     */
    @FXML
    private void takeOrder() {
        List<Order> selectedOrders = availableTable.getSelectionModel().getSelectedItems();
        if (selectedOrders == null || selectedOrders.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select at least one order to take.");
            alert.showAndWait();
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Selection");
        confirm.setHeaderText(null);
        confirm.setContentText("Do you want to take " + selectedOrders.size() + " order(s)?");
        if (confirm.showAndWait().get() != ButtonType.OK) {
            return;
        }
        
        String query = "UPDATE OrderInfo SET carrier_id = ? WHERE id = ? AND (carrier_id = 0 OR carrier_id IS NULL)";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        int successCount = 0;
        int failCount = 0;
        
        try {
            for (Order order : selectedOrders) {
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, carrierId);
                    stmt.setInt(2, order.getId());
                    int rows = stmt.executeUpdate();
                    if (rows > 0) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    failCount++;
                }
            }
            
            Alert result = new Alert(Alert.AlertType.INFORMATION);
            result.setTitle("Result");
            result.setHeaderText(null);
            result.setContentText("✓ Successfully took " + successCount + " order(s)." +
                    (failCount > 0 ? "\n✗ " + failCount + " order(s) were already taken by another carrier." : ""));
            result.showAndWait();
            
            refreshTables();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Marks selected orders as completed (delivered).
     */
    @FXML
    private void completeOrder() {
        List<Order> selectedOrders = myTable.getSelectionModel().getSelectedItems();
        if (selectedOrders == null || selectedOrders.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select at least one order to mark as delivered.");
            alert.showAndWait();
            return;
        }
        
        Dialog<LocalDateTime> dialog = new Dialog<>();
        dialog.setTitle("Complete Delivery");
        dialog.setHeaderText("Mark " + selectedOrders.size() + " order(s) as delivered");
        
        ButtonType confirmButtonType = new ButtonType("Confirm Delivery", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        Label infoLabel = new Label("Enter the actual delivery date and time:");
        infoLabel.setStyle("-fx-font-weight: bold;");
        
        DatePicker datePicker = new DatePicker(java.time.LocalDate.now());
        
        HBox timeBox = new HBox(10);
        Spinner<Integer> hourSpinner = new Spinner<>(0, 23, LocalDateTime.now().getHour());
        hourSpinner.setEditable(true);
        hourSpinner.setPrefWidth(70);
        
        Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, LocalDateTime.now().getMinute());
        minuteSpinner.setEditable(true);
        minuteSpinner.setPrefWidth(70);
        
        timeBox.getChildren().addAll(
            new Label("Hour:"), hourSpinner,
            new Label("Minute:"), minuteSpinner
        );
        
        content.getChildren().addAll(infoLabel, new Label("Date:"), datePicker, timeBox);
        dialog.getDialogPane().setContent(content);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return LocalDateTime.of(
                    datePicker.getValue(),
                    java.time.LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue())
                );
            }
            return null;
        });
        
        dialog.showAndWait().ifPresent(deliveryDateTime -> {
            String query = "UPDATE OrderInfo SET isdelivered = TRUE, deliverytime = ? WHERE id = ?";
            Connection conn = DatabaseAdapter.getInstance().getConnection();
            int successCount = 0;
            
            try {
                for (Order order : selectedOrders) {
                    try (PreparedStatement stmt = conn.prepareStatement(query)) {
                        stmt.setTimestamp(1, Timestamp.valueOf(deliveryDateTime));
                        stmt.setInt(2, order.getId());
                        stmt.executeUpdate();
                        successCount++;
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                
                Alert result = new Alert(Alert.AlertType.INFORMATION);
                result.setTitle("Success");
                result.setHeaderText(null);
                result.setContentText("✓ Successfully marked " + successCount + " order(s) as delivered!");
                result.showAndWait();
                
                refreshTables();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void viewAvailableDetails() {
        viewDetails(availableTable);
    }
    
    @FXML
    private void viewMyDetails() {
        viewDetails(myTable);
    }
    
    @FXML
    private void viewCompletedDetails() {
        viewDetails(completedTable);
    }

    private void viewDetails(TableView<Order> table) {
        Order selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("Please select an order to view details.");
            alert.showAndWait();
            return;
        }
        showOrderDetails(selected);
    }
    
    private void showOrderDetails(Order order) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Order Details - #" + order.getId());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #f5f5f5;");
        
        VBox customerBox = new VBox(8);
        customerBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        Label customerTitle = new Label("👤 Customer Information");
        customerTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        String customerName = getCustomerName(order.getUserId());
        String customerAddress = getCustomerAddress(order.getUserId());
        
        Label nameLabel = new Label("Name: " + customerName);
        Label addressLabel = new Label("Address: " + customerAddress);
        addressLabel.setWrapText(true);
        customerBox.getChildren().addAll(customerTitle, nameLabel, addressLabel);
        
        VBox orderBox = new VBox(8);
        orderBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        Label orderTitle = new Label("📦 Order Information");
        orderTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label orderIdLabel = new Label("Order ID: #" + order.getId());
        Label orderTimeLabel = new Label("Order Time: " + 
            (order.getOrderTime() != null ? order.getOrderTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "N/A"));
        Label deliveryTimeLabel = new Label("Requested Delivery: " + 
            (order.getDeliveryTime() != null ? order.getDeliveryTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) : "N/A"));
        
        orderBox.getChildren().addAll(orderTitle, orderIdLabel, orderTimeLabel, deliveryTimeLabel);
        
        VBox productsBox = new VBox(8);
        productsBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        Label productsTitle = new Label("🛒 Products");
        productsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        TextArea productsArea = new TextArea(getProductDetails(order.getProductsJson()));
        productsArea.setEditable(false);
        productsArea.setPrefRowCount(8);
        productsArea.setWrapText(true);
        productsArea.setStyle("-fx-background-color: #fafafa;");
        
        productsBox.getChildren().addAll(productsTitle, productsArea);
        
        VBox financialBox = new VBox(8);
        financialBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 5;");
        Label financialTitle = new Label("💰 Financial Details");
        financialTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        double subtotal = order.getTotalCost() / 1.18; // Assuming 18% VAT
        double vat = order.getTotalCost() - subtotal;
        
        Label subtotalLabel = new Label(String.format("Subtotal: $%.2f", subtotal));
        Label vatLabel = new Label(String.format("V.A.T (18%%): $%.2f", vat));
        Label totalLabel = new Label(String.format("Total (incl. VAT): $%.2f", order.getTotalCost()));
        totalLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #2e7d32;");
        
        financialBox.getChildren().addAll(financialTitle, subtotalLabel, vatLabel, totalLabel);
        
        content.getChildren().addAll(customerBox, orderBox, productsBox, financialBox);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefSize(500, 600);
        
        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
    }
    
    private String getProductDetails(String productsString) {
        if (productsString == null || productsString.isEmpty()) return "No products";
        
        StringBuilder details = new StringBuilder();
        String[] items = productsString.split(";");
        int index = 1;
        
        for (String item : items) {
            item = item.trim();
            if (item.isEmpty()) continue;
            
            try {
                String[] parts = item.split(":");
                if (parts.length == 2) {
                    String productName = parts[0].trim();
                    double amount = Double.parseDouble(parts[1].trim());
                    details.append(String.format("%d. %s - %.1f kg\n", index++, productName, amount));
                }
            } catch (Exception e) {
                details.append(index++).append(". [Error parsing: ").append(item).append("]\n");
            }
        }
        
        if (details.length() == 0) {
            return "No valid products found";
        }
        
        return details.toString();
    }
    
    @FXML
    private void handleLogout() {
        try {
            com.group27.utils.UserSession.getInstance().clearSession();
            Stage stage = (Stage) availableTable.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            stage.setScene(new Scene(loader.load(), 960, 540));
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
