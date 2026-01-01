package com.group27.model;

import java.io.InputStream;

public class Product {
    private int id;
    private String name;
    private String type; // "Vegetable" or "Fruit"
    private double price;
    private double stock;
    private double threshold;
    private InputStream imageStream; // For BLOB handling

    public Product(int id, String name, String type, double price, double stock, double threshold, InputStream imageStream) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.price = price;
        this.stock = stock;
        this.threshold = threshold;
        this.imageStream = imageStream;
    }
    
    public Product(String name, String type, double price, double stock, double threshold, InputStream imageStream) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.stock = stock;
        this.threshold = threshold;
        this.imageStream = imageStream;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public double getPrice() { return price; }
    public double getStock() { return stock; }
    public double getThreshold() { return threshold; }
    public InputStream getImageStream() { return imageStream; }

    public void setStock(double stock) { this.stock = stock; }
    public void setPrice(double price) { this.price = price; }
}
