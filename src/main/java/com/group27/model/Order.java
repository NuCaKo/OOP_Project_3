package com.group27.model;

import java.time.LocalDateTime;

public class Order {
    private int id;
    private int userId;
    private int carrierId;
    private LocalDateTime orderTime;
    private LocalDateTime deliveryTime;
    private String productsJson; // Stored as CLOB/Text
    private double totalCost;
    private boolean isDelivered;

    public Order(int id, int userId, int carrierId, LocalDateTime orderTime, LocalDateTime deliveryTime, String productsJson, double totalCost, boolean isDelivered) {
        this.id = id;
        this.userId = userId;
        this.carrierId = carrierId;
        this.orderTime = orderTime;
        this.deliveryTime = deliveryTime;
        this.productsJson = productsJson;
        this.totalCost = totalCost;
        this.isDelivered = isDelivered;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getCarrierId() { return carrierId; }
    public LocalDateTime getOrderTime() { return orderTime; }
    public LocalDateTime getDeliveryTime() { return deliveryTime; }
    public String getProductsJson() { return productsJson; }
    public double getTotalCost() { return totalCost; }
    public boolean isDelivered() { return isDelivered; }
    
    public void setCarrierId(int carrierId) { this.carrierId = carrierId; }
    public void setDelivered(boolean delivered) { isDelivered = delivered; }
}
