package com.productivity.dao;

import com.productivity.config.DatabaseConfig;
import com.productivity.model.Department;
import com.productivity.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DepartmentDao {

    public List<Department> findAll() throws SQLException {
        String sql = "SELECT id, name, created_at FROM departments ORDER BY name";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            List<Department> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    public Department findById(int id) throws SQLException {
        String sql = "SELECT id, name, created_at FROM departments WHERE id = ?";
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

    public int insert(Department d) throws SQLException {
        String sql = "INSERT INTO departments (name) VALUES (?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, d.getName());
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
            throw new SQLException("Insert department failed, no ID.");
        } finally {
            DBUtil.close(keys, ps, conn);
        }
    }

    public boolean update(Department d) throws SQLException {
        String sql = "UPDATE departments SET name = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, d.getName());
            ps.setInt(2, d.getId());
            return ps.executeUpdate() > 0;
        } finally {
            DBUtil.close(ps, conn);
        }
    }

    private static Department mapRow(ResultSet rs) throws SQLException {
        Department d = new Department();
        d.setId(rs.getInt("id"));
        d.setName(rs.getString("name"));
        Timestamp ts = rs.getTimestamp("created_at");
        d.setCreatedAt(ts != null ? ts.toLocalDateTime() : null);
        return d;
    }
}
