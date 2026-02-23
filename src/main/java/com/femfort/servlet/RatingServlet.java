package com.femfort.servlet;

import com.femfort.dao.RatingDAO;
import com.femfort.model.Rating;
import com.femfort.util.GsonProvider;
import com.google.gson.JsonSyntaxException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/api/ratings")
public class RatingServlet extends HttpServlet {

    private RatingDAO ratingDAO = new RatingDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String zoneIdParam = req.getParameter("zoneId");
        if (zoneIdParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Missing zoneId parameter\"}");
            return;
        }

        try {
            int zoneId = Integer.parseInt(zoneIdParam);
            var ratings = ratingDAO.findByZoneId(zoneId);
            String json = GsonProvider.get().toJson(ratings);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(json);
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid zoneId format\"}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Database error\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            BufferedReader reader = req.getReader();
            Rating rating = GsonProvider.get().fromJson(reader, Rating.class);

            // Score is deprecated, so we don't validate it here.
            // Severity Level should be validated instead (1-5).
            if (rating == null || rating.getZoneId() <= 0 || rating.getSeverityLevel() < 1
                    || rating.getSeverityLevel() > 5) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid rating data\"}");
                return;
            }

            // Basic Rate Limiting / Spam check could go here (e.g. check session or IP)

            ratingDAO.createRating(rating);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write("{\"message\": \"Rating submitted successfully\"}");

        } catch (JsonSyntaxException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid JSON\"}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Database error\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            BufferedReader reader = req.getReader();
            Rating rating = GsonProvider.get().fromJson(reader, Rating.class);

            if (rating == null || rating.getId() <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid rating ID\"}");
                return;
            }

            // We use the 'isValid' field from the payload to toggle spam status
            // The payload might be { "id": 123, "isValid": false } (Mark as Spam)
            // or { "id": 123, "isValid": true } (Unmark Spam)
            ratingDAO.updateValidity(rating.getId(), rating.isValid());

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"Rating validity updated successfully\"}");

        } catch (JsonSyntaxException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid JSON\"}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Database error\"}");
            e.printStackTrace();
        }
    }
}
