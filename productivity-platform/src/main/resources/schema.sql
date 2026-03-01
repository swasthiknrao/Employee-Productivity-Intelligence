-- Employee Productivity Intelligence Platform - MySQL Schema
-- Run this script to create the database and tables.

CREATE DATABASE IF NOT EXISTS productivity_db;
USE productivity_db;

-- Departments
CREATE TABLE departments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_dept_name (name)
);

-- Activity types (coding, meeting, email, break, etc.)
CREATE TABLE activity_types (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    is_focus_type TINYINT(1) DEFAULT 0,
    UNIQUE KEY uk_activity_name (name)
);

-- Employees
CREATE TABLE employees (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL,
    department_id INT NOT NULL,
    role VARCHAR(80) DEFAULT 'employee',
    join_date DATE NOT NULL,
    is_active TINYINT(1) DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE RESTRICT,
    UNIQUE KEY uk_employee_email (email)
);

CREATE INDEX idx_employees_department ON employees(department_id);
CREATE INDEX idx_employees_active ON employees(is_active);

-- Work sessions (app usage, time blocks)
CREATE TABLE work_sessions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    activity_type_id INT NOT NULL,
    app_name VARCHAR(100) DEFAULT NULL,
    notes VARCHAR(500) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (activity_type_id) REFERENCES activity_types(id) ON DELETE RESTRICT
);

CREATE INDEX idx_work_sessions_employee ON work_sessions(employee_id);
CREATE INDEX idx_work_sessions_times ON work_sessions(start_time, end_time);

-- Tasks
CREATE TABLE tasks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    title VARCHAR(300) NOT NULL,
    status ENUM('pending', 'in_progress', 'completed') DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME DEFAULT NULL,
    estimated_mins INT DEFAULT NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

CREATE INDEX idx_tasks_employee ON tasks(employee_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_tasks_created ON tasks(created_at);

-- Communications (flow metric)
CREATE TABLE communications (
    id INT AUTO_INCREMENT PRIMARY KEY,
    from_employee_id INT NOT NULL,
    to_employee_id INT NOT NULL,
    channel ENUM('email', 'chat') DEFAULT 'chat',
    sent_at DATETIME NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (from_employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    FOREIGN KEY (to_employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

CREATE INDEX idx_communications_from ON communications(from_employee_id);
CREATE INDEX idx_communications_sent ON communications(sent_at);

-- Productivity snapshots (computed scores)
CREATE TABLE productivity_snapshots (
    id INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    snapshot_date DATE NOT NULL,
    score DECIMAL(5,2) NOT NULL,
    tasks_done INT DEFAULT 0,
    focus_minutes INT DEFAULT 0,
    collaboration_count INT DEFAULT 0,
    raw_metrics_json VARCHAR(1000) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    UNIQUE KEY uk_employee_date (employee_id, snapshot_date)
);

CREATE INDEX idx_snapshots_employee ON productivity_snapshots(employee_id);
CREATE INDEX idx_snapshots_date ON productivity_snapshots(snapshot_date);

-- Audit log (privacy/compliance)
CREATE TABLE audit_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_role VARCHAR(50) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) DEFAULT NULL,
    entity_id VARCHAR(50) DEFAULT NULL,
    performed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_performed ON audit_log(performed_at);
