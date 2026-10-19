-- RouteWise Flyway Migration V2: Development Sample Seed Data
-- Demo user password is 'Password123!' (BCrypt hash)

INSERT INTO users (id, email, password_hash, full_name, company_name, role, created_at, updated_at)
VALUES 
(1, 'demo@routewise.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'Sarah Jenkins', 'Metro Courier Express', 'ROLE_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'john.doe@logistics.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.AQubh4a', 'John Doe', 'Rapid Delivery Co', 'ROLE_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Reset sequence if needed
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));

INSERT INTO user_preferences (id, user_id, default_depot_address, default_depot_lat, default_depot_lng, distance_unit, default_speed_kmh, avoid_tolls, theme, created_at, updated_at)
VALUES
(1, 1, 'Central Hub, 500 Howard St, San Francisco, CA', 37.7885, -122.3995, 'km', 40.0, false, 'light', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 2, 'Bay Depot, 100 1st St, San Francisco, CA', 37.7912, -122.3978, 'km', 40.0, false, 'light', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

SELECT setval('user_preferences_id_seq', (SELECT MAX(id) FROM user_preferences));

-- Sample Delivery Orders for User 1 (San Francisco Bay Area)
INSERT INTO delivery_orders (id, user_id, customer_name, address, latitude, longitude, contact_phone, notes, priority, status, created_at, updated_at)
VALUES
(1, 1, 'Blue Bottle Coffee Roastery', '315 Linden St, San Francisco, CA', 37.7763, -122.4231, '+1-415-555-0142', 'Deliver to rear loading bay before 10 AM', 'HIGH', 'DELIVERED', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),
(2, 1, 'SF General Supplies', '1001 Potrero Ave, San Francisco, CA', 37.7558, -122.4047, '+1-415-555-0189', 'Call on arrival for gate code #4412', 'MEDIUM', 'DELIVERED', CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days'),
(3, 1, 'Mission District Medical Clinic', '2450 Mission St, San Francisco, CA', 37.7587, -122.4191, '+1-415-555-0213', 'Temperature sensitive medication package', 'URGENT', 'ASSIGNED', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP),
(4, 1, 'Embarcadero Tech Labs', '201 Mission St, San Francisco, CA', 37.7915, -122.3972, '+1-415-555-0391', 'Suite 1400 - Front desk signature required', 'MEDIUM', 'ASSIGNED', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP),
(5, 1, 'Fishermans Wharf Gift Shop', '2801 Leavenworth St, San Francisco, CA', 37.8080, -122.4177, '+1-415-555-0784', 'Pallet delivery, forklift needed', 'LOW', 'ASSIGNED', CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP),
(6, 1, 'Chinatown Culinary Mart', '744 Washington St, San Francisco, CA', 37.7951, -122.4055, '+1-415-555-0456', 'Fragile porcelain tableware', 'HIGH', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 1, 'Presidio Community Center', '210 Lincoln Blvd, San Francisco, CA', 37.7997, -122.4638, '+1-415-555-0812', 'Deliver packages to main office', 'LOW', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 1, 'Sunset District Hardware', '1320 9th Ave, San Francisco, CA', 37.7634, -122.4662, '+1-415-555-0994', 'Heavy items, dolly required', 'MEDIUM', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

SELECT setval('delivery_orders_id_seq', (SELECT MAX(id) FROM delivery_orders));

-- Sample Route for User 1
INSERT INTO routes (id, user_id, name, depot_address, depot_lat, depot_lng, end_depot_address, end_depot_lat, end_depot_lng, round_trip, optimization_method, total_distance_km, estimated_duration_min, route_geometry, is_estimated, baseline_distance_km, baseline_duration_min, distance_saved_km, savings_percentage, created_at, updated_at)
VALUES
(1, 1, 'Morning Downtown Express', 'Central Hub, 500 Howard St, San Francisco, CA', 37.7885, -122.3995, 'Central Hub, 500 Howard St, San Francisco, CA', 37.7885, -122.3995, true, 'HEURISTIC_2OPT', 14.8, 38.5, '[[37.7885,-122.3995],[37.7915,-122.3972],[37.8080,-122.4177],[37.7587,-122.4191],[37.7885,-122.3995]]', false, 21.2, 55.0, 6.4, 30.19, CURRENT_TIMESTAMP - INTERVAL '1 day', CURRENT_TIMESTAMP - INTERVAL '1 day')
ON CONFLICT (id) DO NOTHING;

SELECT setval('routes_id_seq', (SELECT MAX(id) FROM routes));

-- Link assigned deliveries to route 1
UPDATE delivery_orders SET assigned_route_id = 1 WHERE id IN (3, 4, 5);

-- Sample Route Stops for Route 1
INSERT INTO route_stops (id, route_id, delivery_order_id, stop_sequence, address, latitude, longitude, customer_name, contact_phone, distance_to_next_km, duration_to_next_min)
VALUES
(1, 1, 4, 1, '201 Mission St, San Francisco, CA', 37.7915, -122.3972, 'Embarcadero Tech Labs', '+1-415-555-0391', 2.3, 7.5),
(2, 1, 5, 2, '2801 Leavenworth St, San Francisco, CA', 37.8080, -122.4177, 'Fishermans Wharf Gift Shop', '+1-415-555-0784', 6.8, 16.0),
(3, 1, 3, 3, '2450 Mission St, San Francisco, CA', 37.7587, -122.4191, 'Mission District Medical Clinic', '+1-415-555-0213', 4.5, 12.0)
ON CONFLICT (id) DO NOTHING;

SELECT setval('route_stops_id_seq', (SELECT MAX(id) FROM route_stops));

-- Sample Route History
INSERT INTO route_history (id, user_id, route_id, action, notes, recorded_at)
VALUES
(1, 1, 1, 'CREATED', 'Route Morning Downtown Express created and optimized using 2-Opt heuristic', CURRENT_TIMESTAMP - INTERVAL '1 day'),
(2, 1, 1, 'DISPATCHED', 'Route dispatched to vehicle Van-04', CURRENT_TIMESTAMP - INTERVAL '22 hours')
ON CONFLICT (id) DO NOTHING;

SELECT setval('route_history_id_seq', (SELECT MAX(id) FROM route_history));
