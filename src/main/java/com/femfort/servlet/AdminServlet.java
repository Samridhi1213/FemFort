package com.femfort.servlet;

import com.femfort.dao.DatabaseConnectionManager;
import com.femfort.util.GsonProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Map;

@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("adminUser") == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String pathInfo = req.getPathInfo();
        if ("/moderate".equals(pathInfo)) {
            handleModeration(req, resp);
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleModeration(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            BufferedReader reader = req.getReader();
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = GsonProvider.get().fromJson(reader, Map.class);

            Double ratingIdDouble = (Double) payload.get("ratingId");
            int ratingId = ratingIdDouble.intValue();
            String action = (String) payload.get("action"); // MARK_SPAM, UNMARK_SPAM

            boolean isValid = !"MARK_SPAM".equals(action);

            String sql = "UPDATE ratings SET is_valid = ? WHERE id = ?";
            try (Connection conn = DatabaseConnectionManager.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setBoolean(1, isValid);
                pstmt.setInt(2, ratingId);
                int rows = pstmt.executeUpdate();

                if (rows > 0) {
                    resp.setStatus(HttpServletResponse.SC_OK);
                    resp.getWriter().write("{\"message\": \"Moderation applied\"}");
                } else {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\": \"Rating not found\"}");
                }
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            e.printStackTrace();
        }
    }
}
