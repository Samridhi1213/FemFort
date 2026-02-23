package com.femfort.model;

import java.sql.Timestamp;

public class Rating {
    private int id;
    private int userId;
    private int zoneId;
    private int threatCategoryId;
    private int severityLevel;
    private int score;
    private String comment;
    private boolean isValid;
    private Timestamp createdAt;

    public Rating() {
    }

    public Rating(int userId, int zoneId, int score, String comment) {
        this.userId = userId;
        this.zoneId = zoneId;
        this.score = score;
        this.comment = comment;
        this.isValid = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getZoneId() {
        return zoneId;
    }

    public void setZoneId(int zoneId) {
        this.zoneId = zoneId;
    }

    public int getThreatCategoryId() {
        return threatCategoryId;
    }

    public void setThreatCategoryId(int threatCategoryId) {
        this.threatCategoryId = threatCategoryId;
    }

    public int getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(int severityLevel) {
        this.severityLevel = severityLevel;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
