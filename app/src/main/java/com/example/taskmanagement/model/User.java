package com.example.taskmanagement.model;

import java.io.Serializable;

/**
 * Model representing a registered application user.
 */
public class User implements Serializable {
    private long id;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private long createdAt;

    public User() {
    }

    public User(long id, String username, String password, String fullName, String email, long createdAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.createdAt = createdAt;
    }

    public User(String username, String password, String fullName, String email) {
        this(-1, username, password, fullName, email, System.currentTimeMillis());
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName != null && !fullName.trim().isEmpty() ? fullName : username;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
