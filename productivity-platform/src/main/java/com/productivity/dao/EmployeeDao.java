package com.productivity.dao;

import com.productivity.config.DatabaseConfig;
import com.productivity.model.Employee;
import com.productivity.util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDao {

    public List<Employee> findAll() throws SQLException {
        String sql = "SELECT e.id, e.name, e.email, e.department_id, e.role, e.join_date, e.is_active, e.created_at, d.name AS department_name " +
                     "FROM employees e LEFT JOIN departments d ON e.department_id = d.id WHERE e.is_active = 1 ORDER BY e.name";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            List<Employee> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    public List<Employee> findByDepartmentId(int departmentId) throws SQLException {
        String sql = "SELECT e.id, e.name, e.email, e.department_id, e.role, e.join_date, e.is_active, e.created_at, d.name AS department_name " +
                     "FROM employees e LEFT JOIN departments d ON e.department_id = d.id WHERE e.department_id = ? AND e.is_active = 1 ORDER BY e.name";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, departmentId);
            rs = ps.executeQuery();
            List<Employee> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    public Employee findById(int id) throws SQLException {
        String sql = "SELECT e.id, e.name, e.email, e.department_id, e.role, e.join_date, e.is_active, e.created_at, d.name AS department_name " +
                     "FROM employees e LEFT JOIN departments d ON e.department_id = d.id WHERE e.id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    public int insert(Employee e) throws SQLException {
        String sql = "INSERT INTO employees (name, email, department_id, role, join_date, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, e.getName());
            ps.setString(2, e.getEmail());
            ps.setInt(3, e.getDepartmentId());
            ps.setString(4, e.getRole() != null ? e.getRole() : "employee");
            ps.setObject(5, e.getJoinDate());
            ps.setBoolean(6, e.isActive());
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
            throw new SQLException("Insert employee failed, no ID.");
        } finally {
            DBUtil.close(keys, ps, conn);
        }
    }

    public boolean update(Employee e) throws SQLException {
        String sql = "UPDATE employees SET name = ?, email = ?, department_id = ?, role = ?, join_date = ?, is_active = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, e.getName());
            ps.setString(2, e.getEmail());
            ps.setInt(3, e.getDepartmentId());
            ps.setString(4, e.getRole() != null ? e.getRole() : "employee");
            ps.setObject(5, e.getJoinDate());
            ps.setBoolean(6, e.isActive());
            ps.setInt(7, e.getId());
            return ps.executeUpdate() > 0;
        } finally {
            DBUtil.close(ps, conn);
        }
    }

    private static Employee mapRow(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setId(rs.getInt("id"));
        e.setName(rs.getString("name"));
        e.setEmail(rs.getString("email"));
        e.setDepartmentId(rs.getInt("department_id"));
        e.setRole(rs.getString("role"));
        Date d = rs.getDate("join_date");
        e.setJoinDate(d != null ? d.toLocalDate() : null);
        e.setActive(rs.getBoolean("is_active"));
        Timestamp ts = rs.getTimestamp("created_at");
        e.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        e.setDepartmentName(rs.getString("department_name"));
        return e;
    }
}
