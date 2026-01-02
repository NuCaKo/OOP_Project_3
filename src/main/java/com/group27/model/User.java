package com.group27.model;

public class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private String address;
    private int loyaltyPoints;

    public User(int id, String username, String password, String role, String address) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = address;
        this.loyaltyPoints = 0;
    }
    
    public User(int id, String username, String password, String role, String address, int loyaltyPoints) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = address;
        this.loyaltyPoints = loyaltyPoints;
    }

    public User(String username, String password, String role, String address) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = address;
        this.loyaltyPoints = 0;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getAddress() { return address; }
    public int getLoyaltyPoints() { return loyaltyPoints; }

    public void setAddress(String address) { this.address = address; }
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
}
