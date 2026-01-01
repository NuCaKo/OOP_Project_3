package com.group27.controller;

import com.group27.core.DatabaseAdapter;
import com.group27.model.Order;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;

public class CarrierController {

    @FXML private TableView<Order> availableTable;
    @FXML private TableColumn<Order, Number> avIdCol;
    @FXML private TableColumn<Order, String> avDateCol;
    @FXML private TableColumn<Order, Number> avTotalCol;

    @FXML private TableView<Order> myTable;
    @FXML private TableColumn<Order, Number> myIdCol;
    @FXML private TableColumn<Order, String> myDateCol;
    @FXML private TableColumn<Order, Number> myTotalCol;

    @FXML private TableView<Order> completedTable;
    @FXML private TableColumn<Order, Number> cpIdCol;
    @FXML private TableColumn<Order, String> cpDateCol;

    private ObservableList<Order> availableList = FXCollections.observableArrayList();
    private ObservableList<Order> myList = FXCollections.observableArrayList();
    private ObservableList<Order> completedList = FXCollections.observableArrayList();
    
    private int carrierId;

    @FXML
    public void initialize() {
        com.group27.model.User user = com.group27.utils.UserSession.getInstance().getCurrentUser();
        this.carrierId = (user != null) ? user.getId() : 0;
        
        setupTables();
        refreshTables();
    }
    
    private void setupTables() {
        avIdCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        avDateCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDeliveryTime().toString()));
        avTotalCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTotalCost()));
        availableTable.setItems(availableList);
        
        myIdCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        myDateCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDeliveryTime().toString()));
        myTotalCol.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTotalCost()));
        myTable.setItems(myList);
        
        cpIdCol.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getId()));
        cpDateCol.setCellValueFactory(d -> new SimpleObjectProperty<>(d.getValue().getDeliveryTime() != null ? d.getValue().getDeliveryTime().toString() : ""));
        completedTable.setItems(completedList);
    }
    
    private void refreshTables() {
        availableList.clear();
        myList.clear();
        completedList.clear();
        
        String query = "SELECT * FROM OrderInfo";
        try (Connection conn = DatabaseAdapter.getInstance().getConnection();
             ResultSet rs = conn.createStatement().executeQuery(query)) {
            while (rs.next()) {
                LocalDateTime ot = rs.getTimestamp("ordertime") != null ? rs.getTimestamp("ordertime").toLocalDateTime() : null;
                LocalDateTime dt = rs.getTimestamp("deliverytime") != null ? rs.getTimestamp("deliverytime").toLocalDateTime() : null;
                
                Order o = new Order(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getInt("carrier_id"), // 0 if null/default
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

    @FXML
    private void takeOrder() {
        Order selected = availableTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        // Fix: Concurrency check (only take if carrier_id is 0 or NULL)
        String query = "UPDATE OrderInfo SET carrier_id = ? WHERE id = ? AND (carrier_id = 0 OR carrier_id IS NULL)";
        try (Connection conn = DatabaseAdapter.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, carrierId);
            stmt.setInt(2, selected.getId());
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                 // Order already taken
                 Alert alert = new Alert(Alert.AlertType.WARNING);
                 alert.setTitle("Error");
                 alert.setContentText("This order has already been taken by another carrier.");
                 alert.showAndWait();
            }
            refreshTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    private void completeOrder() {
        Order selected = myTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        
        String query = "UPDATE OrderInfo SET isdelivered = TRUE WHERE id = ?";
        try (Connection conn = DatabaseAdapter.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, selected.getId());
            stmt.executeUpdate();
            refreshTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
