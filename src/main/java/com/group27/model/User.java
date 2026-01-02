package com.group27.model;

/**
 * Represents a user in the system.
 */
public class User {
    private int id;
    private String username;
    private String password;
    private String role;
    private String address;
    private int loyaltyPoints;

    /**
     * Constructs a new User.
     *
     * @param id            The user's ID.
     * @param username      The user's username.
     * @param password      The user's hashed password.
     * @param role          The user's role (e.g., customer, owner, carrier).
     * @param address       The user's address.
     * @param loyaltyPoints The user's loyalty points.
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
     * Gets the user ID.
     *
     * @return The user ID.
     */
    public int getId() { return id; }

    /**
     * Gets the username.
     *
     * @return The username.
     */
    public String getUsername() { return username; }

    /**
     * Gets the hashed password.
     *
     * @return The hashed password.
     */
    public String getPassword() { return password; }

    /**
     * Gets the user role.
     *
     * @return The user role.
     */
    public String getRole() { return role; }

    /**
     * Gets the user address.
     *
     * @return The user address.
     */
    public String getAddress() { return address; }

    /**
     * Sets the user address.
     *
     * @param address The new address.
     */
    public void setAddress(String address) { this.address = address; }

    /**
     * Gets the loyalty points.
     *
     * @return The loyalty points.
     */
    public int getLoyaltyPoints() { return loyaltyPoints; }

    /**
     * Sets the loyalty points.
     *
     * @param points The new loyalty points balance.
     */
    public void setLoyaltyPoints(int points) { this.loyaltyPoints = points; }
}
