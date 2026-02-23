package com.femfort.model;

import java.sql.Timestamp;

public class Zone {
    private int id;
    private int campusId;
    private String name;
    private String description;
    private double lat;
    private double lng;
    private int radiusMeters;
    private int baseRiskLevel;
    private String dominantThreatType;
    private Timestamp createdAt;

    // Computed fields
    private double currentSafetyScore;
    private int ratingCount;
    private boolean needsReview;
    private Timestamp lastRatedAt;

    public Zone() {
    }

    public Zone(int id, String name, String description, double lat, double lng, int radiusMeters, int baseRiskLevel) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
        this.radiusMeters = radiusMeters;
        this.baseRiskLevel = baseRiskLevel;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCampusId() {
        return campusId;
    }

    public void setCampusId(int campusId) {
        this.campusId = campusId;
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

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public int getRadiusMeters() {
        return radiusMeters;
    }

    public void setRadiusMeters(int radiusMeters) {
        this.radiusMeters = radiusMeters;
    }

    public int getBaseRiskLevel() {
        return baseRiskLevel;
    }

    public void setBaseRiskLevel(int baseRiskLevel) {
        this.baseRiskLevel = baseRiskLevel;
    }

    public String getDominantThreatType() {
        return dominantThreatType;
    }

    public void setDominantThreatType(String dominantThreatType) {
        this.dominantThreatType = dominantThreatType;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public double getCurrentSafetyScore() {
        return currentSafetyScore;
    }

    public void setCurrentSafetyScore(double currentSafetyScore) {
        this.currentSafetyScore = currentSafetyScore;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public boolean isNeedsReview() {
        return needsReview;
    }

    public void setNeedsReview(boolean needsReview) {
        this.needsReview = needsReview;
    }

    public Timestamp getLastRatedAt() {
        return lastRatedAt;
    }

    public void setLastRatedAt(Timestamp lastRatedAt) {
        this.lastRatedAt = lastRatedAt;
    }
}
