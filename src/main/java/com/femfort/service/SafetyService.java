package com.femfort.service;

import com.femfort.dao.ZoneDAO;
import com.femfort.model.Zone;
import java.sql.SQLException;
import java.util.List;

public class SafetyService {

    private ZoneDAO zoneDAO = new ZoneDAO();

    public List<Zone> getAllZonesWithSafetyScores(int campusId) throws SQLException {
        return zoneDAO.findAll(campusId);
    }

    // Future: Add more complex aggregation logic here if not handled by SQL View
    // e.g., weighting recent ratings higher
}
