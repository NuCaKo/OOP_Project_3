package com.group27.model;

import java.io.InputStream;

/**
 * Represents a product in the GreenGrocer system.
 * Contains product information including ID, name, type, price, stock level,
 * threshold for dynamic pricing, and product image.
 * 
 * @author Group27
 * @version 1.0
 */
public class Product {
    private int id;
    private String name;
    private String type; // "Vegetable" or "Fruit"
    private double price;
    private double stock;
    private double threshold;
    private InputStream imageStream; // For BLOB handling

    /**
     * Constructs a Product with all parameters including ID.
     * 
     * @param id The unique identifier for the product
     * @param name The name of the product
     * @param type The type of product (Vegetable or Fruit)
     * @param price The base price of the product
     * @param stock The current stock level
     * @param threshold The stock threshold for dynamic pricing
     * @param imageStream InputStream containing the product image
     */
    public Product(int id, String name, String type, double price, double stock, double threshold, InputStream imageStream) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.price = price;
        this.stock = stock;
        this.threshold = threshold;
        this.imageStream = imageStream;
    }
    
    /**
     * Constructs a Product without an ID (for new product creation).
     * ID will be assigned by the database.
     * 
     * @param name The name of the product
     * @param type The type of product (Vegetable or Fruit)
     * @param price The base price of the product
     * @param stock The current stock level
     * @param threshold The stock threshold for dynamic pricing
     * @param imageStream InputStream containing the product image
     */
    public Product(String name, String type, double price, double stock, double threshold, InputStream imageStream) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.stock = stock;
        this.threshold = threshold;
        this.imageStream = imageStream;
    }

    /**
     * Gets the product's unique identifier.
     * 
     * @return The product ID
     */
    public int getId() { return id; }
    
    /**
     * Gets the product name.
     * 
     * @return The product name
     */
    public String getName() { return name; }
    
    /**
     * Gets the product type.
     * 
     * @return The product type (Vegetable or Fruit)
     */
    public String getType() { return type; }
    
    /**
     * Gets the base price of the product.
     * 
     * @return The base price
     */
    public double getPrice() { return price; }
    
    /**
     * Gets the current stock level.
     * 
     * @return The stock level
     */
    public double getStock() { return stock; }
    
    /**
     * Gets the stock threshold for dynamic pricing.
     * 
     * @return The threshold value
     */
    public double getThreshold() { return threshold; }
    
    /**
     * Gets the product image as an InputStream.
     * 
     * @return InputStream containing the product image
     */
    public InputStream getImageStream() { return imageStream; }

    /**
     * Sets the stock level of the product.
     * 
     * @param stock The new stock level
     */
    public void setStock(double stock) { this.stock = stock; }
    
    /**
     * Sets the price of the product.
     * 
     * @param price The new price
     */
    public void setPrice(double price) { this.price = price; }
}
