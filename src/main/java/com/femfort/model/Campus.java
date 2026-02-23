package com.femfort.model;

import java.sql.Timestamp;

public class Campus {
    private int id;
    private String name;
    private String description;
    private double centerLat;
    private double centerLng;
    private Timestamp createdAt;

    public Campus() {
    }

    public Campus(int id, String name, String description, double centerLat, double centerLng) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.centerLat = centerLat;
        this.centerLng = centerLng;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCenterLat() {
        return centerLat;
    }

    public void setCenterLat(double centerLat) {
        this.centerLat = centerLat;
    }

    public double getCenterLng() {
        return centerLng;
    }

    public void setCenterLng(double centerLng) {
        this.centerLng = centerLng;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
