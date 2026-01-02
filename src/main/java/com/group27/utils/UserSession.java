package com.group27.utils;

import com.group27.model.User;

/**
 * Singleton class to manage the current user session.
 */
public class UserSession {
    private static UserSession instance;
    private User currentUser;

    /**
     * Private constructor to prevent instantiation.
     */
    private UserSession() {}

    /**
     * Returns the singleton instance of UserSession.
     *
     * @return The singleton instance.
     */
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    /**
     * Sets the current logged-in user.
     *
     * @param user The user to set as current.
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Gets the current logged-in user.
     *
     * @return The current user, or null if no user is logged in.
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Clears the current session (logs out the user).
     */
    public void clearSession() {
        currentUser = null;
    }
}
