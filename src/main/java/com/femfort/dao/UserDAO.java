package com.femfort.dao;

import com.femfort.model.User;
import java.sql.*;
import java.util.Optional;

public class UserDAO {

    public User createUser(String alias) throws SQLException {
        String sql = "INSERT INTO users (alias) VALUES (?) RETURNING id, created_at, is_blocked";
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, alias);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setAlias(alias);
                    user.setBlocked(rs.getBoolean("is_blocked"));
                    user.setCreatedAt(rs.getTimestamp("created_at"));
                    return user;
                }
            }
        }
        throw new SQLException("Creating user failed, no ID obtained.");
    }

    public Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User(
                        rs.getInt("id"),
                        rs.getString("alias"),
                        rs.getBoolean("is_blocked"),
                        rs.getTimestamp("created_at")
                    );
                    return Optional.of(user);
                }
            }
        }
        return Optional.empty();
    }
}
