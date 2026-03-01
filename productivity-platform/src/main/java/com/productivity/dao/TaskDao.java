package com.productivity.dao;

import com.productivity.config.DatabaseConfig;
import com.productivity.model.Task;
import com.productivity.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskDao {

    public int insert(Task t) throws SQLException {
        String sql = "INSERT INTO tasks (employee_id, title, status, completed_at, estimated_mins) VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, t.getEmployeeId());
            ps.setString(2, t.getTitle());
            ps.setString(3, t.getStatus().name());
            ps.setObject(4, t.getCompletedAt());
            ps.setObject(5, t.getEstimatedMins());
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
            throw new SQLException("Insert task failed, no ID.");
        } finally {
            DBUtil.close(keys, ps, conn);
        }
    }

    public boolean update(Task t) throws SQLException {
        String sql = "UPDATE tasks SET title = ?, status = ?, completed_at = ?, estimated_mins = ? WHERE id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, t.getTitle());
            ps.setString(2, t.getStatus().name());
            ps.setObject(3, t.getCompletedAt());
            ps.setObject(4, t.getEstimatedMins());
            ps.setInt(5, t.getId());
            return ps.executeUpdate() > 0;
        } finally {
            DBUtil.close(ps, conn);
        }
    }

    public Task findById(int id) throws SQLException {
        String sql = "SELECT t.id, t.employee_id, t.title, t.status, t.created_at, t.completed_at, t.estimated_mins, e.name AS employee_name " +
                     "FROM tasks t LEFT JOIN employees e ON t.employee_id = e.id WHERE t.id = ?";
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

    public List<Task> findByEmployeeId(int employeeId) throws SQLException {
        String sql = "SELECT t.id, t.employee_id, t.title, t.status, t.created_at, t.completed_at, t.estimated_mins, e.name AS employee_name " +
                     "FROM tasks t LEFT JOIN employees e ON t.employee_id = e.id WHERE t.employee_id = ? ORDER BY t.created_at DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, employeeId);
            rs = ps.executeQuery();
            List<Task> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    public List<Task> findByEmployeeIdAndCreatedSince(int employeeId, LocalDateTime since) throws SQLException {
        String sql = "SELECT t.id, t.employee_id, t.title, t.status, t.created_at, t.completed_at, t.estimated_mins, e.name AS employee_name " +
                     "FROM tasks t LEFT JOIN employees e ON t.employee_id = e.id WHERE t.employee_id = ? AND t.created_at >= ? ORDER BY t.created_at DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, employeeId);
            ps.setObject(2, since);
            rs = ps.executeQuery();
            List<Task> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    public List<Task> findAll(int limit) throws SQLException {
        String sql = "SELECT t.id, t.employee_id, t.title, t.status, t.created_at, t.completed_at, t.estimated_mins, e.name AS employee_name " +
                     "FROM tasks t LEFT JOIN employees e ON t.employee_id = e.id ORDER BY t.created_at DESC LIMIT ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, limit);
            rs = ps.executeQuery();
            List<Task> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** Total tasks for employee with created_at >= since. */
    public int countByEmployeeSince(int employeeId, LocalDateTime since) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE employee_id = ? AND created_at >= ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, employeeId);
            ps.setObject(2, since);
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** Completed tasks for employee with completed_at >= since. */
    public int countCompletedByEmployeeSince(int employeeId, LocalDateTime since) throws SQLException {
        String sql = "SELECT COUNT(*) FROM tasks WHERE employee_id = ? AND status = 'completed' AND completed_at >= ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, employeeId);
            ps.setObject(2, since);
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    private static Task mapRow(ResultSet rs) throws SQLException {
        Task t = new Task();
        t.setId(rs.getInt("id"));
        t.setEmployeeId(rs.getInt("employee_id"));
        t.setTitle(rs.getString("title"));
        String statusStr = rs.getString("status");
        try {
            t.setStatus(statusStr != null ? Task.Status.valueOf(statusStr) : Task.Status.pending);
        } catch (IllegalArgumentException e) {
            t.setStatus(Task.Status.pending);
        }
        t.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        t.setCompletedAt(rs.getObject("completed_at", LocalDateTime.class));
        int em = rs.getInt("estimated_mins");
        t.setEstimatedMins(rs.wasNull() ? null : em);
        t.setEmployeeName(rs.getString("employee_name"));
        return t;
    }
}
