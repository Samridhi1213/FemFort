package com.femfort.service;

import com.femfort.dao.RouteDAO;
import com.femfort.dao.ZoneDAO;
import com.femfort.model.Path;
import com.femfort.model.Zone;

import java.sql.SQLException;
import java.util.*;

public class RoutingService {

    private RouteDAO routeDAO = new RouteDAO();
    private ZoneDAO zoneDAO = new ZoneDAO();

    public List<Zone> findSafestRoute(int startZoneId, int endZoneId, int campusId) throws SQLException {
        List<Zone> allZones = zoneDAO.findAll(campusId);
        List<Path> allPaths = routeDAO.findAllPaths();

        Map<Integer, Zone> zoneMap = new HashMap<>();
        for (Zone z : allZones)
            zoneMap.put(z.getId(), z);

        Map<Integer, List<Path>> adjList = new HashMap<>();

        for (Path p : allPaths) {
            adjList.computeIfAbsent(p.getFromZoneId(), k -> new ArrayList<>()).add(p);
        }

        int kNearest = 3;
        double maxConnectionDist = 400.0; // Reduced threshold to discourage long direct jumps

        for (Zone z1 : allZones) {
            List<ZoneDistance> neighbors = new ArrayList<>();
            for (Zone z2 : allZones) {
                if (z1.getId() == z2.getId())
                    continue;
                double dist = calculateDistance(z1.getLat(), z1.getLng(), z2.getLat(), z2.getLng());
                if (dist <= maxConnectionDist) {
                    neighbors.add(new ZoneDistance(z2, dist));
                }
            }

            neighbors.sort(Comparator.comparingDouble(nd -> nd.distance));

            for (int i = 0; i < Math.min(neighbors.size(), kNearest); i++) {
                ZoneDistance target = neighbors.get(i);
                Zone z2 = target.zone;
                double dist = target.distance;

                Path p = new Path();
                p.setFromZoneId(z1.getId());
                p.setToZoneId(z2.getId());
                p.setDistanceMeters(dist);
                p.setBaseRiskLevel(1);
                adjList.computeIfAbsent(z1.getId(), k -> new ArrayList<>()).add(p);
            }
        }

        // Dijkstra's Algorithm
        PriorityQueue<NodeWrapper> pq = new PriorityQueue<>(Comparator.comparingDouble(nw -> nw.cost));
        Map<Integer, Double> minCost = new HashMap<>();
        Map<Integer, Integer> previous = new HashMap<>();

        pq.add(new NodeWrapper(startZoneId, 0.0));
        minCost.put(startZoneId, 0.0);

        while (!pq.isEmpty()) {
            NodeWrapper current = pq.poll();
            int u = current.nodeId;

            if (u == endZoneId)
                break; // Found target

            if (current.cost > minCost.getOrDefault(u, Double.MAX_VALUE))
                continue;

            if (adjList.containsKey(u)) {
                for (Path edge : adjList.get(u)) {
                    int v = edge.getToZoneId();

                    // Calculate Weight: Heavily penalize risk to force detours through safe zones
                    Zone targetZone = zoneMap.get(v);
                    // currentSafetyScore now holds Average Severity (1=Safe, 5=Risky)
                    double averageSeverity = targetZone != null ? targetZone.getCurrentSafetyScore() : 2.5;
                    if (averageSeverity == 0)
                        averageSeverity = 2.5; // Handle unrated

                    // Risk is now directly the severity level
                    double zoneRisk = averageSeverity;

                    double riskMultiplier = 10.0; // Heavily penalize risk
                    double effectiveDistance = edge.getDistanceMeters() * (1 + (zoneRisk * riskMultiplier));

                    if (averageSeverity > 3.0) {
                        effectiveDistance *= 5.0; // Avoid at all costs
                    }

                    if (averageSeverity <= 2.0) {
                        effectiveDistance *= 0.5; // Stronger bonus for safe zones
                    }

                    double newCost = minCost.get(u) + effectiveDistance;

                    if (newCost < minCost.getOrDefault(v, Double.MAX_VALUE)) {
                        minCost.put(v, newCost);
                        previous.put(v, u);
                        pq.add(new NodeWrapper(v, newCost));
                    }
                }
            }
        }

        // Reconstruct Path
        List<Zone> path = new ArrayList<>();
        Integer curr = endZoneId;
        if (!previous.containsKey(curr) && startZoneId != endZoneId) {
            return Collections.emptyList(); // No path found
        }

        while (curr != null) {
            path.add(0, zoneMap.get(curr));
            curr = previous.get(curr);
        }

        return path;
    }

    private static class ZoneDistance {
        Zone zone;
        double distance;

        ZoneDistance(Zone zone, double distance) {
            this.zone = zone;
            this.distance = distance;
        }
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        return distance;
    }

    private static class NodeWrapper {
        int nodeId;
        double cost;

        NodeWrapper(int nodeId, double cost) {
            this.nodeId = nodeId;
            this.cost = cost;
        }
    }
}
