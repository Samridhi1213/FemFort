-- Drop existing objects to ensure clean schema
DROP VIEW IF EXISTS zone_safety_stats CASCADE;
DROP TABLE IF EXISTS location_requests CASCADE;
DROP TABLE IF EXISTS admin_actions CASCADE;
DROP TABLE IF EXISTS ratings CASCADE;
DROP TABLE IF EXISTS paths CASCADE;
DROP TABLE IF EXISTS zones CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS threat_categories CASCADE;
DROP TABLE IF EXISTS campuses CASCADE;

-- Campuses table
CREATE TABLE campuses (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    center_lat DOUBLE PRECISION NOT NULL,
    center_lng DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Threat Categories table
CREATE TABLE threat_categories (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL, -- e.g., 'Heavy Construction', 'Overcrowded'
    shape_type VARCHAR(20) NOT NULL -- 'square', 'circle', 'triangle'
);

-- Users table
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    alias VARCHAR(50) NOT NULL,
    is_blocked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Zones table (Nodes in the graph)
CREATE TABLE zones (
    id SERIAL PRIMARY KEY,
    campus_id INTEGER REFERENCES campuses(id),
    name VARCHAR(100) NOT NULL,
    description TEXT,
    lat DOUBLE PRECISION NOT NULL,
    lng DOUBLE PRECISION NOT NULL,
    radius_meters INTEGER DEFAULT 50,
    base_risk_level INTEGER DEFAULT 1, -- 1 (Safe) to 5 (High Risk)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Paths table (Edges in the graph)
CREATE TABLE paths (
    id SERIAL PRIMARY KEY,
    from_zone_id INTEGER REFERENCES zones(id),
    to_zone_id INTEGER REFERENCES zones(id),
    distance_meters DOUBLE PRECISION NOT NULL,
    base_risk_level INTEGER DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Ratings table
CREATE TABLE ratings (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    zone_id INTEGER REFERENCES zones(id),
    threat_category_id INTEGER REFERENCES threat_categories(id),
    severity_level INTEGER CHECK (severity_level >= 1 AND severity_level <= 5),
    score INTEGER CHECK (score >= 1 AND score <= 5), -- Kept for backward compatibility/general safety score
    comment TEXT,
    is_valid BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Admin Actions table
CREATE TABLE admin_actions (
    id SERIAL PRIMARY KEY,
    admin_username VARCHAR(50),
    rating_id INTEGER REFERENCES ratings(id),
    action_type VARCHAR(50), -- MARK_SPAM, UNMARK_SPAM, BLOCK_USER
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Location Requests table
CREATE TABLE location_requests (
    id SERIAL PRIMARY KEY,
    request_type VARCHAR(20) NOT NULL, -- 'ZONE' or 'CAMPUS'
    campus_id INTEGER REFERENCES campuses(id), -- Nullable if requesting a new campus
    name VARCHAR(100) NOT NULL,
    description TEXT,
    lat DOUBLE PRECISION NOT NULL,
    lng DOUBLE PRECISION NOT NULL,
    user_comment TEXT,
    status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- View for Zone Safety Stats
CREATE VIEW zone_safety_stats AS
SELECT 
    z.id AS zone_id,
    z.name,
    z.campus_id,
    COUNT(r.id) AS rating_count,
    COALESCE(AVG(r.score) FILTER (WHERE r.is_valid = TRUE), 0) AS average_score,
    MAX(r.created_at) AS last_rated_at
FROM zones z
LEFT JOIN ratings r ON z.id = r.zone_id
GROUP BY z.id;
