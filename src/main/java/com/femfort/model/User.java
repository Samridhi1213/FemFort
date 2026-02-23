package com.femfort.model;

import java.sql.Timestamp;

public class User {
    private int id;
    private String alias;
    private boolean isBlocked;
    private Timestamp createdAt;

    public User() {}

    public User(int id, String alias, boolean isBlocked, Timestamp createdAt) {
        this.id = id;
        this.alias = alias;
        this.isBlocked = isBlocked;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public boolean isBlocked() { return isBlocked; }
    public void setBlocked(boolean blocked) { isBlocked = blocked; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
