package com.femfort.servlet;

import com.femfort.dao.ZoneDAO;
import com.femfort.model.Zone;
import com.femfort.util.GsonProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/zones")
public class ZoneServlet extends HttpServlet {

    private ZoneDAO zoneDAO = new ZoneDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String campusIdStr = req.getParameter("campusId");
        if (campusIdStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Missing campusId\"}");
            return;
        }

        try {
            int campusId = Integer.parseInt(campusIdStr);
            List<Zone> zones = zoneDAO.findAll(campusId);
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(GsonProvider.get().toJson(zones));
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid campusId\"}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Database error\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Direct creation is disabled. Users must submit a request via /api/requests
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        resp.getWriter().write("{\"error\": \"Direct zone creation is disabled. Please submit a location request.\"}");
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Missing id parameter\"}");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            zoneDAO.deleteZone(id);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"Zone deleted successfully\"}");
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid id format\"}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Database error during deletion\"}");
            e.printStackTrace();
        }
    }
}
