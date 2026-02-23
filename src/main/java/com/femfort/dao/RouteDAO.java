package com.femfort.dao;

import com.femfort.model.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RouteDAO {

    public List<Path> findAllPaths() throws SQLException {
        List<Path> paths = new ArrayList<>();
        String sql = "SELECT * FROM paths";
        
        try (Connection conn = DatabaseConnectionManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Path path = new Path();
                path.setId(rs.getInt("id"));
                path.setFromZoneId(rs.getInt("from_zone_id"));
                path.setToZoneId(rs.getInt("to_zone_id"));
                path.setDistanceMeters(rs.getDouble("distance_meters"));
                path.setBaseRiskLevel(rs.getInt("base_risk_level"));
                path.setCreatedAt(rs.getTimestamp("created_at"));
                paths.add(path);
            }
        }
        return paths;
    }
}
