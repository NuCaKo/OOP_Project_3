package com.group27.model;

import javafx.scene.image.Image;

/**
 * Represents a product in the store.
 */
public class Product {
    private int id;
    private String name;
    private String type;
    private double price;
    private double stock;
    private double threshold;
    private Image image;

    /**
     * Constructs a new Product.
     *
     * @param id        The product ID.
     * @param name      The product name.
     * @param type      The product type (e.g., Vegetable, Fruit).
     * @param price     The price per unit.
     * @param stock     The available stock quantity.
     * @param threshold The stock threshold for dynamic pricing.
     * @param image     The product image (optional).
     */
    public Product(int id, String name, String type, double price, double stock, double threshold, Image image) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.price = price;
        this.stock = stock;
        this.threshold = threshold;
        this.image = image;
    }

    // Getters and Setters

    /**
     * Gets the product ID.
     *
     * @return The product ID.
     */
    public int getId() { return id; }

    /**
     * Gets the product name.
     *
     * @return The product name.
     */
    public String getName() { return name; }

    /**
     * Gets the product type.
     *
     * @return The product type.
     */
    public String getType() { return type; }

    /**
     * Gets the product price.
     *
     * @return The product price.
     */
    public double getPrice() { return price; }

    /**
     * Gets the current stock level.
     *
     * @return The stock level.
     */
    public double getStock() { return stock; }

    /**
     * Sets the stock level.
     *
     * @param stock The new stock level.
     */
    public void setStock(double stock) { this.stock = stock; }

    /**
     * Gets the stock threshold for dynamic pricing.
     *
     * @return The stock threshold.
     */
    public double getThreshold() { return threshold; }

    /**
     * Gets the product image.
     *
     * @return The product image.
     */
    public Image getImage() { return image; }
}
