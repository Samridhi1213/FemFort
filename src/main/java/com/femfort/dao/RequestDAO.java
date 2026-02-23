package com.femfort.dao;

import com.femfort.model.LocationRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RequestDAO {

    public void createRequest(LocationRequest request) throws SQLException {
        String sql = "INSERT INTO location_requests (request_type, campus_id, name, description, lat, lng, user_comment, status) VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING')";
        try (Connection conn = DatabaseConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, request.getRequestType());
            if (request.getCampusId() > 0) {
                pstmt.setInt(2, request.getCampusId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setString(3, request.getName());
            pstmt.setString(4, request.getDescription());
            pstmt.setDouble(5, request.getLat());
            pstmt.setDouble(6, request.getLng());
            pstmt.setString(7, request.getUserComment());

            pstmt.executeUpdate();
        }
    }

    public List<LocationRequest> findAllPending() throws SQLException {
        List<LocationRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM location_requests WHERE status = 'PENDING' ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                LocationRequest req = new LocationRequest();
                req.setId(rs.getInt("id"));
                req.setRequestType(rs.getString("request_type"));
                req.setCampusId(rs.getInt("campus_id"));
                req.setName(rs.getString("name"));
                req.setDescription(rs.getString("description"));
                req.setLat(rs.getDouble("lat"));
                req.setLng(rs.getDouble("lng"));
                req.setUserComment(rs.getString("user_comment"));
                req.setStatus(rs.getString("status"));
                req.setCreatedAt(rs.getTimestamp("created_at"));
                requests.add(req);
            }
        }
        return requests;
    }

    public LocationRequest findById(int id) throws SQLException {
        String sql = "SELECT * FROM location_requests WHERE id = ?";
        try (Connection conn = DatabaseConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    LocationRequest req = new LocationRequest();
                    req.setId(rs.getInt("id"));
                    req.setRequestType(rs.getString("request_type"));
                    req.setCampusId(rs.getInt("campus_id"));
                    req.setName(rs.getString("name"));
                    req.setDescription(rs.getString("description"));
                    req.setLat(rs.getDouble("lat"));
                    req.setLng(rs.getDouble("lng"));
                    req.setUserComment(rs.getString("user_comment"));
                    req.setStatus(rs.getString("status"));
                    req.setCreatedAt(rs.getTimestamp("created_at"));
                    return req;
                }
            }
        }
        return null;
    }

    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE location_requests SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnectionManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();
        }
    }
}
