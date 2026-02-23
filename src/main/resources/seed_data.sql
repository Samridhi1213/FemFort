-- Seed Campuses
INSERT INTO campuses (name, description, center_lat, center_lng) VALUES
('Galgotias University', 'Greater Noida Campus', 28.365850, 77.541227);

-- Seed Threat Categories
INSERT INTO threat_categories (name, shape_type) VALUES
('Heavy construction based threat', 'square'),
('Overcrowded', 'circle'),
('Lighting and visibility', 'triangle');

-- Seed Users
INSERT INTO users (alias) VALUES ('Student_A'), ('Campus_Security'), ('Jane_Doe');

-- Seed Zones (Galgotias University)
INSERT INTO zones (campus_id, name, description, lat, lng, radius_meters, base_risk_level) VALUES 
(1, 'A Block', 'Main Academic Block', 28.36499220107086, 77.54136759867008, 60, 1),
(1, 'B Block', 'Engineering Block', 28.3655, 77.5420, 50, 1),
(1, 'Cafeteria', 'Central Canteen', 28.3660, 77.5415, 40, 1),
(1, 'Parking lot', 'Outdoor Sports Area', 28.3670, 77.5400, 80, 2),
(1, 'C Block', 'engineering block', 28.366392587541778, 77.54250491982316, 50, 2);

-- Seed Paths (Connecting zones)
INSERT INTO paths (from_zone_id, to_zone_id, distance_meters, base_risk_level) VALUES 
(1, 2, 100, 1), -- A Block <-> C Block
(2, 1, 100, 1),
(2, 3, 80, 1),  -- C Block <-> Cafeteria
(3, 2, 80, 1),
(3, 4, 150, 2), -- Cafeteria <-> Sports Ground
(4, 3, 150, 2);

-- Seed Ratings (With Threat Categories)
INSERT INTO ratings (user_id, zone_id, threat_category_id, severity_level, score, comment, is_valid) VALUES 
(1, 1, 3, 1, 5, 'Good lighting near A Block', TRUE), -- Lighting (Triangle), Level 1 (Green)
(2, 4, 1, 4, 2, 'Construction work near ground', TRUE), -- Construction (Square), Level 4 (Red-Orange)
(3, 3, 2, 3, 3, 'Crowded during lunch', TRUE); -- Overcrowded (Circle), Level 3 (Orange)
