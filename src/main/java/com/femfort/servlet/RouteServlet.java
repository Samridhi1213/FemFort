package com.femfort.servlet;

import com.femfort.model.Zone;
import com.femfort.service.RoutingService;
import com.femfort.util.GsonProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/routes")
public class RouteServlet extends HttpServlet {

    private RoutingService routingService = new RoutingService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fromIdStr = req.getParameter("fromZoneId");
        String toIdStr = req.getParameter("toZoneId");
        String campusIdStr = req.getParameter("campusId");

        if (fromIdStr == null || toIdStr == null || campusIdStr == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Missing fromZoneId, toZoneId, or campusId\"}");
            return;
        }

        try {
            int fromId = Integer.parseInt(fromIdStr);
            int toId = Integer.parseInt(toIdStr);
            int campusId = Integer.parseInt(campusIdStr);

            List<Zone> route = routingService.findSafestRoute(fromId, toId, campusId);

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(GsonProvider.get().toJson(route));

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\": \"Invalid ID format\"}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Database error\"}");
            e.printStackTrace();
        }
    }
}
