package com.group27.model;

/**
 * Represents a user in the GreenGrocer system.
 * Contains user information including ID, credentials, role, address, and loyalty points.
 * 
 * @author Group27
 * @version 1.0
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private String address;
    private int loyaltyPoints;

    /**
     * Constructs a User with the specified parameters.
     * Loyalty points are initialized to 0.
     * 
     * @param id The unique identifier for the user
     * @param username The username for login
     * @param password The password for authentication
     * @param role The role of the user (customer, carrier, or owner)
     * @param address The address of the user
     */
    public User(int id, String username, String password, String role, String address) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = address;
        this.loyaltyPoints = 0;
    }
    
    /**
     * Constructs a User with all parameters including loyalty points.
     * 
     * @param id The unique identifier for the user
     * @param username The username for login
     * @param password The password for authentication
     * @param role The role of the user (customer, carrier, or owner)
     * @param address The address of the user
     * @param loyaltyPoints The loyalty points accumulated by the user
     */
    public User(int id, String username, String password, String role, String address, int loyaltyPoints) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = address;
        this.loyaltyPoints = loyaltyPoints;
    }

    /**
     * Constructs a User without an ID (for new user registration).
     * ID will be assigned by the database. Loyalty points are initialized to 0.
     * 
     * @param username The username for login
     * @param password The password for authentication
     * @param role The role of the user (customer, carrier, or owner)
     * @param address The address of the user
     */
    public User(String username, String password, String role, String address) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.address = address;
        this.loyaltyPoints = 0;
    }

    /**
     * Gets the user's unique identifier.
     * 
     * @return The user ID
     */
    public int getId() { return id; }
    
    /**
     * Gets the username.
     * 
     * @return The username
     */
    public String getUsername() { return username; }
    
    /**
     * Gets the password.
     * 
     * @return The password
     */
    public String getPassword() { return password; }
    
    /**
     * Gets the user's role.
     * 
     * @return The role (customer, carrier, or owner)
     */
    public String getRole() { return role; }
    
    /**
     * Gets the user's address.
     * 
     * @return The address
     */
    public String getAddress() { return address; }
    
    /**
     * Gets the user's loyalty points.
     * 
     * @return The loyalty points
     */
    public int getLoyaltyPoints() { return loyaltyPoints; }

    /**
     * Sets the user's address.
     * 
     * @param address The new address
     */
    public void setAddress(String address) { this.address = address; }
    
    /**
     * Sets the user's loyalty points.
     * 
     * @param loyaltyPoints The new loyalty points value
     */
    public void setLoyaltyPoints(int loyaltyPoints) { this.loyaltyPoints = loyaltyPoints; }
}
