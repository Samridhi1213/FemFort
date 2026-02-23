package com.femfort.dao;

import com.femfort.model.Zone;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ZoneDAO {

    public List<Zone> findAll(int campusId) throws SQLException {
        List<Zone> zones = new ArrayList<>();
        // Join with the view to get stats, and subquery for dominant threat
        // We need to calculate average SEVERITY now.
        // The view zone_safety_stats might calculate average_score based on 'score'
        // column.
        // We should probably update the view or just do a direct query here for
        // simplicity since we are changing logic.
        // Let's do a direct query to be safe and flexible.
        String sql = "SELECT z.*, " +
                "COALESCE(AVG(r.severity_level) FILTER (WHERE r.is_valid = TRUE), 0) as average_severity, " +
                "COUNT(r.id) FILTER (WHERE r.is_valid = TRUE) as rating_count, " +
                "MAX(r.created_at) FILTER (WHERE r.is_valid = TRUE) as last_rated_at, " +
                "(SELECT tc.name FROM ratings r2 " +
                " JOIN threat_categories tc ON r2.threat_category_id = tc.id " +
                " WHERE r2.zone_id = z.id AND r2.is_valid = TRUE " +
                " GROUP BY tc.name " +
                " ORDER BY COUNT(*) DESC LIMIT 1) as dominant_threat " +
                "FROM zones z " +
                "LEFT JOIN ratings r ON z.id = r.zone_id " +
                "WHERE z.campus_id = ? " +
                "GROUP BY z.id";

        try (Connection conn = DatabaseConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, campusId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    zones.add(mapResultSetToZone(rs));
                }
            }
        }
        return zones;
    }

    public Optional<Zone> findById(int id) throws SQLException {
        String sql = "SELECT z.*, s.average_score, s.rating_count, s.last_rated_at " +
                "FROM zones z " +
                "LEFT JOIN zone_safety_stats s ON z.id = s.zone_id " +
                "WHERE z.id = ?";

        try (Connection conn = DatabaseConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToZone(rs));
                }
            }
        }
        return Optional.empty();
    }

    private Zone mapResultSetToZone(ResultSet rs) throws SQLException {
        Zone zone = new Zone();
        zone.setId(rs.getInt("id"));
        zone.setCampusId(rs.getInt("campus_id"));
        zone.setName(rs.getString("name"));
        zone.setDescription(rs.getString("description"));
        zone.setLat(rs.getDouble("lat"));
        zone.setLng(rs.getDouble("lng"));
        zone.setRadiusMeters(rs.getInt("radius_meters"));
        zone.setBaseRiskLevel(rs.getInt("base_risk_level"));
        zone.setCreatedAt(rs.getTimestamp("created_at"));

        // Computed fields
        // We map average_severity to currentSafetyScore field to avoid renaming
        // everything right now
        // But semantically, this is now SEVERITY (1=Low/Safe, 5=High/Risky)
        zone.setCurrentSafetyScore(rs.getDouble("average_severity"));
        zone.setRatingCount(rs.getInt("rating_count"));
        zone.setLastRatedAt(rs.getTimestamp("last_rated_at"));
        zone.setDominantThreatType(rs.getString("dominant_threat"));

        // Needs review if severity is high (> 3) or few ratings
        boolean needsReview = zone.getRatingCount() < 3 || zone.getCurrentSafetyScore() > 3.0;
        zone.setNeedsReview(needsReview);

        return zone;
    }

    public int createZone(Zone zone) throws SQLException {
        String sql = "INSERT INTO zones (campus_id, name, description, lat, lng, radius_meters, base_risk_level) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, zone.getCampusId());
            pstmt.setString(2, zone.getName());
            pstmt.setString(3, zone.getDescription());
            pstmt.setDouble(4, zone.getLat());
            pstmt.setDouble(5, zone.getLng());
            pstmt.setInt(6, zone.getRadiusMeters());
            pstmt.setInt(7, zone.getBaseRiskLevel());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating zone failed, no ID obtained.");
                }
            }
        }
    }

    public void deleteZone(int zoneId) throws SQLException {
        String sqlRatings = "DELETE FROM ratings WHERE zone_id = ?";
        String sqlPathsFrom = "DELETE FROM paths WHERE from_zone_id = ?";
        String sqlPathsTo = "DELETE FROM paths WHERE to_zone_id = ?";
        String sqlZone = "DELETE FROM zones WHERE id = ?";

        try (Connection conn = DatabaseConnectionManager.getConnection()) {
            conn.setAutoCommit(false); // Transaction

            try (PreparedStatement pstmt = conn.prepareStatement(sqlRatings)) {
                pstmt.setInt(1, zoneId);
                pstmt.executeUpdate();
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sqlPathsFrom)) {
                pstmt.setInt(1, zoneId);
                pstmt.executeUpdate();
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sqlPathsTo)) {
                pstmt.setInt(1, zoneId);
                pstmt.executeUpdate();
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sqlZone)) {
                pstmt.setInt(1, zoneId);
                pstmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Re-throw to let servlet handle error
        }
    }
}
