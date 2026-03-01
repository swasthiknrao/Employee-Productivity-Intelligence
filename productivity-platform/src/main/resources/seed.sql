-- Seed data for demo. Run after schema.sql (once).
USE productivity_db;

INSERT IGNORE INTO departments (id, name) VALUES
(1, 'Engineering'),
(2, 'Product'),
(3, 'Support');

INSERT IGNORE INTO activity_types (id, name, is_focus_type) VALUES
(1, 'coding', 1),
(2, 'meeting', 0),
(3, 'email', 0),
(4, 'break', 0),
(5, 'research', 1),
(6, 'documentation', 1);

INSERT IGNORE INTO employees (id, name, email, department_id, role, join_date, is_active) VALUES
(1, 'Alice Chen', 'alice@company.com', 1, 'developer', '2023-01-15', 1),
(2, 'Bob Smith', 'bob@company.com', 1, 'developer', '2023-03-01', 1),
(3, 'Carol Doe', 'carol@company.com', 2, 'product_manager', '2022-06-01', 1),
(4, 'Dave Wilson', 'dave@company.com', 3, 'support_lead', '2023-02-10', 1);

INSERT IGNORE INTO work_sessions (employee_id, start_time, end_time, activity_type_id, app_name, notes) VALUES
(1, DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 9 HOUR, DATE_SUB(NOW(), INTERVAL 6 DAY) + INTERVAL 11 HOUR, 1, 'IDE', 'Sprint task A'),
(1, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 14 HOUR, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 15 HOUR, 2, 'Meet', 'Standup'),
(2, DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 10 HOUR, DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 12 HOUR, 1, 'IDE', NULL),
(1, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 9 HOUR, DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 12 HOUR, 1, 'IDE', 'Feature X'),
(2, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 8 HOUR, DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 9 HOUR, 2, 'Meet', 'Planning');

INSERT IGNORE INTO tasks (employee_id, title, status, completed_at, estimated_mins) VALUES
(1, 'Implement login API', 'completed', DATE_SUB(NOW(), INTERVAL 3 DAY), 120),
(1, 'Add unit tests for auth', 'completed', DATE_SUB(NOW(), INTERVAL 1 DAY), 60),
(1, 'Refactor dashboard service', 'in_progress', NULL, 180),
(2, 'Fix payment bug', 'completed', DATE_SUB(NOW(), INTERVAL 2 DAY), 90),
(2, 'Update dependencies', 'pending', NULL, 45),
(3, 'Write PRD for feature Y', 'in_progress', NULL, 240),
(4, 'Handle tier-2 tickets', 'completed', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL);

INSERT IGNORE INTO communications (from_employee_id, to_employee_id, channel, sent_at) VALUES
(1, 2, 'chat', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 1, 'chat', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 3, 'email', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 1, 'chat', DATE_SUB(NOW(), INTERVAL 1 DAY)),
(4, 2, 'chat', DATE_SUB(NOW(), INTERVAL 1 DAY));
