package com.group27.model;

import java.time.LocalDateTime;

/**
 * Represents a customer order.
 */
public class Order {
    private int id;
    private LocalDateTime orderTime;
    private LocalDateTime deliveryTime;
    private String products; // Simplified product string (JSON-like)
    private int userId;
    private int carrierId;
    private boolean isDelivered;
    private double totalCost;
    private String invoice;
    private int carrierRating;

    /**
     * Constructs a new Order.
     *
     * @param id            The order ID.
     * @param userId        The ID of the customer who placed the order.
     * @param carrierId     The ID of the carrier assigned (0 if none).
     * @param orderTime     The time the order was placed.
     * @param deliveryTime  The time the order was delivered (can be null).
     * @param products      The string representation of products in the order.
     * @param totalCost     The total cost of the order.
     * @param isDelivered   True if the order is delivered, false otherwise.
     */
    public Order(int id, int userId, int carrierId, LocalDateTime orderTime, LocalDateTime deliveryTime,
                 String products, double totalCost, boolean isDelivered) {
        this.id = id;
        this.userId = userId;
        this.carrierId = carrierId;
        this.orderTime = orderTime;
        this.deliveryTime = deliveryTime;
        this.products = products;
        this.totalCost = totalCost;
        this.isDelivered = isDelivered;
    }

    // Getters
    public int getId() { return id; }
    public LocalDateTime getOrderTime() { return orderTime; }
    public LocalDateTime getDeliveryTime() { return deliveryTime; }
    public String getProductsJson() { return products; } // Renamed to match controller usage
    public int getUserId() { return userId; }
    public int getCarrierId() { return carrierId; }
    public boolean isDelivered() { return isDelivered; }
    public double getTotalCost() { return totalCost; }
    public String getInvoice() { return invoice; }
    public int getCarrierRating() { return carrierRating; }

    // Setters
    public void setDeliveryTime(LocalDateTime deliveryTime) { this.deliveryTime = deliveryTime; }
    public void setCarrierId(int carrierId) { this.carrierId = carrierId; }
    public void setDelivered(boolean delivered) { isDelivered = delivered; }
    public void setCarrierRating(int carrierRating) { this.carrierRating = carrierRating; }
    public void setInvoice(String invoice) { this.invoice = invoice; }
}
