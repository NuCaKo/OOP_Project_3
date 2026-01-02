package com.group27.model;

import java.time.LocalDateTime;

/**
 * Represents an order in the GreenGrocer system.
 * Contains order information including ID, user ID, carrier ID, timestamps,
 * product details (stored as JSON), total cost, and delivery status.
 * 
 * @author Group27
 * @version 1.0
 */
public class Order {
    private int id;
    private int userId;
    private int carrierId;
    private LocalDateTime orderTime;
    private LocalDateTime deliveryTime;
    private String productsJson; // Stored as CLOB/Text
    private double totalCost;
    private boolean isDelivered;

    /**
     * Constructs an Order with all parameters.
     * 
     * @param id The unique identifier for the order
     * @param userId The ID of the user who placed the order
     * @param carrierId The ID of the carrier assigned to deliver the order
     * @param orderTime The timestamp when the order was placed
     * @param deliveryTime The timestamp when the order was/will be delivered
     * @param productsJson JSON string containing the products in the order
     * @param totalCost The total cost of the order
     * @param isDelivered Whether the order has been delivered
     */
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

    /**
     * Gets the order's unique identifier.
     * 
     * @return The order ID
     */
    public int getId() { return id; }
    
    /**
     * Gets the ID of the user who placed the order.
     * 
     * @return The user ID
     */
    public int getUserId() { return userId; }
    
    /**
     * Gets the ID of the carrier assigned to the order.
     * 
     * @return The carrier ID
     */
    public int getCarrierId() { return carrierId; }
    
    /**
     * Gets the timestamp when the order was placed.
     * 
     * @return The order time
     */
    public LocalDateTime getOrderTime() { return orderTime; }
    
    /**
     * Gets the timestamp when the order was/will be delivered.
     * 
     * @return The delivery time
     */
    public LocalDateTime getDeliveryTime() { return deliveryTime; }
    
    /**
     * Gets the JSON string containing product details.
     * 
     * @return The products JSON string
     */
    public String getProductsJson() { return productsJson; }
    
    /**
     * Gets the total cost of the order.
     * 
     * @return The total cost
     */
    public double getTotalCost() { return totalCost; }
    
    /**
     * Checks if the order has been delivered.
     * 
     * @return true if delivered, false otherwise
     */
    public boolean isDelivered() { return isDelivered; }
    
    /**
     * Sets the carrier ID for the order.
     * 
     * @param carrierId The carrier ID to assign
     */
    public void setCarrierId(int carrierId) { this.carrierId = carrierId; }
    
    /**
     * Sets the delivery status of the order.
     * 
     * @param delivered The delivery status
     */
    public void setDelivered(boolean delivered) { isDelivered = delivered; }
}
