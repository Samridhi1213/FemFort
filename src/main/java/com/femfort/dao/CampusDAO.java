package com.femfort.dao;

import com.femfort.model.Campus;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CampusDAO {

    public List<Campus> findAll() throws SQLException {
        List<Campus> campuses = new ArrayList<>();
        String sql = "SELECT * FROM campuses ORDER BY name";

        try (Connection conn = DatabaseConnectionManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Campus campus = new Campus();
                campus.setId(rs.getInt("id"));
                campus.setName(rs.getString("name"));
                campus.setDescription(rs.getString("description"));
                campus.setCenterLat(rs.getDouble("center_lat"));
                campus.setCenterLng(rs.getDouble("center_lng"));
                campus.setCreatedAt(rs.getTimestamp("created_at"));
                campuses.add(campus);
            }
        }
        return campuses;
    }

    public Campus findById(int id) throws SQLException {
        String sql = "SELECT * FROM campuses WHERE id = ?";
        try (Connection conn = DatabaseConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Campus campus = new Campus();
                    campus.setId(rs.getInt("id"));
                    campus.setName(rs.getString("name"));
                    campus.setDescription(rs.getString("description"));
                    campus.setCenterLat(rs.getDouble("center_lat"));
                    campus.setCenterLng(rs.getDouble("center_lng"));
                    campus.setCreatedAt(rs.getTimestamp("created_at"));
                    return campus;
                }
            }
        }
        return null;
    }

    public int createCampus(Campus campus) throws SQLException {
        String sql = "INSERT INTO campuses (name, description, center_lat, center_lng) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, campus.getName());
            pstmt.setString(2, campus.getDescription());
            pstmt.setDouble(3, campus.getCenterLat());
            pstmt.setDouble(4, campus.getCenterLng());

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating campus failed, no ID obtained.");
                }
            }
        }
    }
}
