package com.femfort.dao;

import com.femfort.model.Rating;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RatingDAO {

    public void createRating(Rating rating) throws SQLException {
        String sql = "INSERT INTO ratings (user_id, zone_id, threat_category_id, severity_level, score, comment) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, rating.getUserId());
            pstmt.setInt(2, rating.getZoneId());
            if (rating.getThreatCategoryId() > 0) {
                pstmt.setInt(3, rating.getThreatCategoryId());
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            if (rating.getSeverityLevel() > 0) {
                pstmt.setInt(4, rating.getSeverityLevel());
            } else {
                pstmt.setNull(4, Types.INTEGER);
            }
            // Score is deprecated in favor of severity. We set it to 3 (neutral) to satisfy
            // CHECK (score >= 1 AND score <= 5).
            pstmt.setInt(5, 3);
            pstmt.setString(6, rating.getComment());

            pstmt.executeUpdate();
        }
    }

    public List<Rating> findByZoneId(int zoneId) throws SQLException {
        List<Rating> ratings = new ArrayList<>();
        String sql = "SELECT * FROM ratings WHERE zone_id = ? AND is_valid = TRUE ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, zoneId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Rating rating = new Rating();
                    rating.setId(rs.getInt("id"));
                    rating.setUserId(rs.getInt("user_id"));
                    rating.setZoneId(rs.getInt("zone_id"));
                    rating.setThreatCategoryId(rs.getInt("threat_category_id"));
                    rating.setSeverityLevel(rs.getInt("severity_level"));
                    rating.setScore(rs.getInt("score"));
                    rating.setComment(rs.getString("comment"));
                    rating.setValid(rs.getBoolean("is_valid"));
                    rating.setCreatedAt(rs.getTimestamp("created_at"));
                    ratings.add(rating);
                }
            }
        }
        return ratings;
    }

    public void updateValidity(int ratingId, boolean isValid) throws SQLException {
        String sql = "UPDATE ratings SET is_valid = ? WHERE id = ?";
        try (Connection conn = DatabaseConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, isValid);
            pstmt.setInt(2, ratingId);
            pstmt.executeUpdate();
        }
    }
}
