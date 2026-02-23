package com.femfort.servlet;

import com.femfort.util.GsonProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;

@WebServlet("/api/auth/login")
public class AuthServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BufferedReader reader = req.getReader();
        @SuppressWarnings("unchecked")
        Map<String, String> credentials = GsonProvider.get().fromJson(reader, Map.class);

        String username = credentials.get("username");
        String password = credentials.get("password");

        // Simple hardcoded check for demo purposes
        if ("admin".equals(username) && "admin123".equals(password)) {
            HttpSession session = req.getSession(true);
            session.setAttribute("adminUser", username);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"message\": \"Login successful\"}");
        } else {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\": \"Invalid credentials\"}");
        }
    }
}
