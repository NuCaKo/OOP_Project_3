package com.group27.controller;

import com.group27.core.DatabaseAdapter;
import com.group27.model.Order;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class OrderController {

    @FXML private ListView<Order> ordersListView;
    private ObservableList<Order> orders = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        loadOrders();

        ordersListView.setItems(orders);
        ordersListView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Order> call(ListView<Order> param) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Order item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            VBox card = new VBox(5);
                            card.getStyleClass().add("liquid-glass-pane");
                            card.setStyle("-fx-padding: 10; -fx-background-color: rgba(255,255,255,0.9);");

                            HBox top = new HBox(10);
                            Label idLbl = new Label("Order #" + item.getId());
                            idLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
                            Label dateLbl = new Label(item.getOrderTime().toString().replace("T", " "));
                            top.getChildren().addAll(idLbl, new Label("|"), dateLbl);

                            HBox mid = new HBox(10);
                            Label statusLbl = new Label(item.isDelivered() ? "Delivered" : "Pending");
                            statusLbl.setStyle(item.isDelivered() ? "-fx-text-fill: green; -fx-font-weight: bold;" : "-fx-text-fill: orange;");
                            Label costLbl = new Label("Total: $" + String.format("%.2f", item.getTotalCost()));
                            mid.getChildren().addAll(statusLbl, new Label("-"), costLbl);

                            HBox actions = new HBox(10);
                            if (!item.isDelivered()) {
                                Button cancelBtn = new Button("Cancel");
                                cancelBtn.setStyle("-fx-background-color: #ffcdd2; -fx-text-fill: #c62828; -fx-font-size: 10px;");
                                cancelBtn.setOnAction(e -> cancelOrder(item));
                                actions.getChildren().add(cancelBtn);
                            } else {
                                Button rateBtn = new Button("Rate");
                                rateBtn.setStyle("-fx-font-size: 10px;");
                                rateBtn.setOnAction(e -> showRateDialog(item));
                                actions.getChildren().add(rateBtn);
                            }

                            card.getChildren().addAll(top, mid, actions);
                            setGraphic(card);
                        }
                    }
                };
            }
        });
    }

    private void loadOrders() {
        orders.clear();
        com.group27.model.User user = com.group27.utils.UserSession.getInstance().getCurrentUser();
        if (user == null) return;

        String query = "SELECT * FROM OrderInfo WHERE user_id = ? ORDER BY ordertime DESC";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, user.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                LocalDateTime ot = rs.getTimestamp("ordertime") != null ? rs.getTimestamp("ordertime").toLocalDateTime() : null;
                LocalDateTime dt = rs.getTimestamp("deliverytime") != null ? rs.getTimestamp("deliverytime").toLocalDateTime() : null;
                orders.add(new Order(
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

    private void cancelOrder(Order order) {
        // Logic similar to CustomerController but cleaner
        // Check 1h window
        if (order.getOrderTime().plusHours(1).isBefore(LocalDateTime.now())) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setContentText("Cannot cancel order after 1 hour.");
            a.show();
            return;
        }

        String query = "DELETE FROM OrderInfo WHERE id = ?";
        Connection conn = DatabaseAdapter.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, order.getId());
            stmt.executeUpdate();
            loadOrders();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showRateDialog(Order order) {
        TextInputDialog dialog = new TextInputDialog("5");
        dialog.setTitle("Rate Carrier");
        dialog.setHeaderText("Rate order #" + order.getId() + " (1-5)");
        dialog.showAndWait().ifPresent(rating -> {
            try {
                int r = Integer.parseInt(rating);
                if (r < 1 || r > 5) throw new NumberFormatException();

                String query = "UPDATE OrderInfo SET carrier_rating = ? WHERE id = ?";
                Connection conn = DatabaseAdapter.getInstance().getConnection();
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, r);
                    stmt.setInt(2, order.getId());
                    stmt.executeUpdate();
                }
            } catch (Exception e) {
                // Ignore invalid input
            }
        });
    }
}
