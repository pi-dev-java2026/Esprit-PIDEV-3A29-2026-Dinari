package Fintech.utils;

import Fintech.entities.User;


public class UserSession {

    private static UserSession instance;
    private User currentUser;

    private UserSession() {
        // Private constructor to prevent instantiation
    }


    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }


    public void setCurrentUser(User user) {
        this.currentUser = user;
    }


    public User getCurrentUser() {
        return currentUser;
    }


    public boolean isLoggedIn() {
        return currentUser != null;
    }


    public boolean isAdmin() {
        return currentUser != null && "Admin".equalsIgnoreCase(currentUser.getRole());
    }


    public void clearSession() {
        currentUser = null;
    }
}
