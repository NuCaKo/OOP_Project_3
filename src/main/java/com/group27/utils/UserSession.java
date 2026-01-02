package com.group27.utils;

import com.group27.model.User;

/**
 * Singleton class that manages the current user session.
 * Provides methods to set, get, and clear the currently logged-in user.
 * 
 * @author Group27
 * @version 1.0
 */
public class UserSession {
    private static UserSession instance;
    private User currentUser;

    /**
     * Private constructor to enforce singleton pattern.
     */
    private UserSession() {}

    /**
     * Returns the singleton instance of UserSession.
     * Creates a new instance if one doesn't exist.
     * 
     * @return The singleton UserSession instance
     */
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Sets the currently logged-in user.
     * 
     * @param user The User object to set as the current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Gets the currently logged-in user.
     * 
     * @return The current User object, or null if no user is logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Clears the current user session by setting currentUser to null.
     * Used when logging out.
     */
    public void clearSession() {
        this.currentUser = null;
    }
}
