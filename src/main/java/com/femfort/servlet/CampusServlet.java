package com.femfort.servlet;

import com.femfort.dao.CampusDAO;
import com.femfort.model.Campus;
import com.femfort.util.GsonProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/campuses")
public class CampusServlet extends HttpServlet {

    private CampusDAO campusDAO = new CampusDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            List<Campus> campuses = campusDAO.findAll();

            resp.setContentType("application/json");
            resp.setCharacterEncoding("UTF-8");
            resp.getWriter().write(GsonProvider.get().toJson(campuses));

        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\": \"Database error\"}");
            e.printStackTrace();
        }
    }
}
