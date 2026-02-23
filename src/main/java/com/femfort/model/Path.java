package com.femfort.model;

import java.sql.Timestamp;

public class Path {
    private int id;
    private int fromZoneId;
    private int toZoneId;
    private double distanceMeters;
    private int baseRiskLevel;
    private Timestamp createdAt;

    public Path() {}

    public Path(int fromZoneId, int toZoneId, double distanceMeters, int baseRiskLevel) {
        this.fromZoneId = fromZoneId;
        this.toZoneId = toZoneId;
        this.distanceMeters = distanceMeters;
        this.baseRiskLevel = baseRiskLevel;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFromZoneId() { return fromZoneId; }
    public void setFromZoneId(int fromZoneId) { this.fromZoneId = fromZoneId; }

    public int getToZoneId() { return toZoneId; }
    public void setToZoneId(int toZoneId) { this.toZoneId = toZoneId; }

    public double getDistanceMeters() { return distanceMeters; }
    public void setDistanceMeters(double distanceMeters) { this.distanceMeters = distanceMeters; }

    public int getBaseRiskLevel() { return baseRiskLevel; }
    public void setBaseRiskLevel(int baseRiskLevel) { this.baseRiskLevel = baseRiskLevel; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
