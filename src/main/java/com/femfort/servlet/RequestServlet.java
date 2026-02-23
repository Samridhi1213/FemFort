package com.femfort.servlet;

import com.femfort.dao.RequestDAO;
import com.femfort.dao.ZoneDAO;
import com.femfort.dao.CampusDAO;
import com.femfort.model.LocationRequest;
import com.femfort.util.GsonProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/requests")
public class RequestServlet extends HttpServlet {

    private RequestDAO requestDAO = new RequestDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // In a real app, check for Admin Auth here
        try {
            List<LocationRequest> requests = requestDAO.findAllPending();
            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(GsonProvider.get().toJson(requests));
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Database error\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            var reader = req.getReader();
            var payload = GsonProvider.get().fromJson(reader, LocationRequest.class);

            if (payload == null || payload.getName() == null || payload.getLat() == 0 || payload.getLng() == 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid request data\"}");
                return;
            }

            // Validate Request Type
            if (!"ZONE".equals(payload.getRequestType()) && !"CAMPUS".equals(payload.getRequestType())) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid request type\"}");
                return;
            }

            // If ZONE, campusId is required
            if ("ZONE".equals(payload.getRequestType()) && payload.getCampusId() <= 0) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Campus ID required for Zone request\"}");
                return;
            }

            requestDAO.createRequest(payload);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().write("{\"message\": \"Request submitted successfully\"}");

        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Database error\"}");
            e.printStackTrace();
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            var reader = req.getReader();
            var payload = GsonProvider.get().fromJson(reader, LocationRequest.class); // Reusing model for ID and Status

            if (payload == null || payload.getId() == 0 || payload.getStatus() == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid request data. ID and Status required.\"}");
                return;
            }

            String newStatus = payload.getStatus().toUpperCase();
            if (!"APPROVED".equals(newStatus) && !"REJECTED".equals(newStatus)) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Invalid status. Must be APPROVED or REJECTED.\"}");
                return;
            }

            LocationRequest existingRequest = requestDAO.findById(payload.getId());
            if (existingRequest == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("{\"error\": \"Request not found.\"}");
                return;
            }

            if (!"PENDING".equals(existingRequest.getStatus())) {
                resp.setStatus(HttpServletResponse.SC_CONFLICT);
                resp.getWriter().write("{\"error\": \"Request is already processed.\"}");
                return;
            }

            int createdId = 0;
            if ("APPROVED".equals(newStatus)) {
                if ("ZONE".equals(existingRequest.getRequestType())) {
                    ZoneDAO zoneDAO = new ZoneDAO();
                    com.femfort.model.Zone newZone = new com.femfort.model.Zone();
                    newZone.setCampusId(existingRequest.getCampusId());
                    newZone.setName(existingRequest.getName());
                    newZone.setDescription(existingRequest.getDescription());
                    newZone.setLat(existingRequest.getLat());
                    newZone.setLng(existingRequest.getLng());
                    newZone.setRadiusMeters(100); // Default radius
                    newZone.setBaseRiskLevel(1); // Default safe

                    createdId = zoneDAO.createZone(newZone);
                } else if ("CAMPUS".equals(existingRequest.getRequestType())) {
                    CampusDAO campusDAO = new CampusDAO();
                    com.femfort.model.Campus newCampus = new com.femfort.model.Campus();
                    newCampus.setName(existingRequest.getName());
                    newCampus.setDescription(existingRequest.getDescription());
                    newCampus.setCenterLat(existingRequest.getLat());
                    newCampus.setCenterLng(existingRequest.getLng());

                    createdId = campusDAO.createCampus(newCampus);
                }
            }

            requestDAO.updateStatus(payload.getId(), newStatus);

            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"Request updated successfully\", \"createdId\": " + createdId + "}");

        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Database error\"}");
            e.printStackTrace();
        }
    }
}
