-- RouteWise PostgreSQL Flyway Migration V1: Initial Schema

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    company_name VARCHAR(150),
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    default_depot_address VARCHAR(255),
    default_depot_lat DOUBLE PRECISION,
    default_depot_lng DOUBLE PRECISION,
    distance_unit VARCHAR(20) DEFAULT 'km',
    default_speed_kmh DOUBLE PRECISION DEFAULT 40.0,
    avoid_tolls BOOLEAN DEFAULT FALSE,
    theme VARCHAR(20) DEFAULT 'light',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS routes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    depot_address VARCHAR(255) NOT NULL,
    depot_lat DOUBLE PRECISION NOT NULL,
    depot_lng DOUBLE PRECISION NOT NULL,
    end_depot_address VARCHAR(255),
    end_depot_lat DOUBLE PRECISION,
    end_depot_lng DOUBLE PRECISION,
    round_trip BOOLEAN DEFAULT TRUE,
    optimization_method VARCHAR(50) NOT NULL,
    total_distance_km DOUBLE PRECISION NOT NULL,
    estimated_duration_min DOUBLE PRECISION NOT NULL,
    route_geometry TEXT,
    is_estimated BOOLEAN DEFAULT FALSE,
    baseline_distance_km DOUBLE PRECISION,
    baseline_duration_min DOUBLE PRECISION,
    distance_saved_km DOUBLE PRECISION DEFAULT 0.0,
    savings_percentage DOUBLE PRECISION DEFAULT 0.0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS delivery_orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    customer_name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    contact_phone VARCHAR(50),
    notes TEXT,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    assigned_route_id BIGINT REFERENCES routes(id) ON DELETE SET NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS route_stops (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    delivery_order_id BIGINT REFERENCES delivery_orders(id) ON DELETE SET NULL,
    stop_sequence INT NOT NULL,
    address VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    customer_name VARCHAR(150),
    contact_phone VARCHAR(50),
    estimated_arrival TIMESTAMP WITH TIME ZONE,
    distance_to_next_km DOUBLE PRECISION,
    duration_to_next_min DOUBLE PRECISION
);

CREATE TABLE IF NOT EXISTS route_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    route_id BIGINT REFERENCES routes(id) ON DELETE SET NULL,
    action VARCHAR(50) NOT NULL,
    notes TEXT,
    recorded_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for optimal lookup and query performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_deliveries_user_status ON delivery_orders(user_id, status);
CREATE INDEX IF NOT EXISTS idx_deliveries_assigned_route ON delivery_orders(assigned_route_id);
CREATE INDEX IF NOT EXISTS idx_routes_user_created ON routes(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_route_stops_route_seq ON route_stops(route_id, stop_sequence);
CREATE INDEX IF NOT EXISTS idx_route_history_user ON route_history(user_id, recorded_at DESC);
