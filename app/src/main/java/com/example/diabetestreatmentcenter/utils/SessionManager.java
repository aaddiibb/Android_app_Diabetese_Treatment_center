package com.example.diabetestreatmentcenter.utils;

import com.example.diabetestreatmentcenter.models.User;

public class SessionManager {
    private static SessionManager instance;
    private User currentUser;
    private String currentUserId;

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public void setCurrentUserId(String userId) {
        this.currentUserId = userId;
    }

    public void clear() {
        this.currentUser = null;
        this.currentUserId = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null && currentUserId != null;
    }
}
