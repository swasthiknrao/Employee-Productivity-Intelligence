package com.productivity.dao;

import com.productivity.config.DatabaseConfig;
import com.productivity.model.ProductivitySnapshot;
import com.productivity.util.DBUtil;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProductivitySnapshotDao {

    public void upsert(ProductivitySnapshot s) throws SQLException {
        String sql = "INSERT INTO productivity_snapshots (employee_id, snapshot_date, score, tasks_done, focus_minutes, collaboration_count, raw_metrics_json) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE score = VALUES(score), tasks_done = VALUES(tasks_done), " +
                     "focus_minutes = VALUES(focus_minutes), collaboration_count = VALUES(collaboration_count), raw_metrics_json = VALUES(raw_metrics_json)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, s.getEmployeeId());
            ps.setObject(2, s.getSnapshotDate());
            ps.setDouble(3, s.getScore());
            ps.setInt(4, s.getTasksDone());
            ps.setInt(5, s.getFocusMinutes());
            ps.setInt(6, s.getCollaborationCount());
            ps.setString(7, s.getRawMetricsJson());
            ps.executeUpdate();
        } finally {
            DBUtil.close(ps, conn);
        }
    }

    public ProductivitySnapshot findByEmployeeAndDate(int employeeId, LocalDate date) throws SQLException {
        String sql = "SELECT ps.id, ps.employee_id, ps.snapshot_date, ps.score, ps.tasks_done, ps.focus_minutes, ps.collaboration_count, ps.raw_metrics_json, ps.created_at, e.name AS employee_name " +
                     "FROM productivity_snapshots ps LEFT JOIN employees e ON ps.employee_id = e.id WHERE ps.employee_id = ? AND ps.snapshot_date = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, employeeId);
            ps.setObject(2, date);
            rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    public List<ProductivitySnapshot> findTopByScore(LocalDate onDate, int limit) throws SQLException {
        String sql = "SELECT ps.id, ps.employee_id, ps.snapshot_date, ps.score, ps.tasks_done, ps.focus_minutes, ps.collaboration_count, ps.raw_metrics_json, ps.created_at, e.name AS employee_name " +
                     "FROM productivity_snapshots ps LEFT JOIN employees e ON ps.employee_id = e.id WHERE ps.snapshot_date = ? ORDER BY ps.score DESC LIMIT ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setObject(1, onDate);
            ps.setInt(2, limit);
            rs = ps.executeQuery();
            List<ProductivitySnapshot> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    public List<ProductivitySnapshot> findBySnapshotDate(LocalDate date) throws SQLException {
        String sql = "SELECT ps.id, ps.employee_id, ps.snapshot_date, ps.score, ps.tasks_done, ps.focus_minutes, ps.collaboration_count, ps.raw_metrics_json, ps.created_at, e.name AS employee_name " +
                     "FROM productivity_snapshots ps LEFT JOIN employees e ON ps.employee_id = e.id WHERE ps.snapshot_date = ? ORDER BY ps.score DESC";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setObject(1, date);
            rs = ps.executeQuery();
            List<ProductivitySnapshot> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    public List<ProductivitySnapshot> findByEmployeeId(int employeeId, int limit) throws SQLException {
        String sql = "SELECT ps.id, ps.employee_id, ps.snapshot_date, ps.score, ps.tasks_done, ps.focus_minutes, ps.collaboration_count, ps.raw_metrics_json, ps.created_at, e.name AS employee_name " +
                     "FROM productivity_snapshots ps LEFT JOIN employees e ON ps.employee_id = e.id WHERE ps.employee_id = ? ORDER BY ps.snapshot_date DESC LIMIT ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, employeeId);
            ps.setInt(2, limit);
            rs = ps.executeQuery();
            List<ProductivitySnapshot> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    private static ProductivitySnapshot mapRow(ResultSet rs) throws SQLException {
        ProductivitySnapshot s = new ProductivitySnapshot();
        s.setId(rs.getInt("id"));
        s.setEmployeeId(rs.getInt("employee_id"));
        s.setSnapshotDate(rs.getObject("snapshot_date", LocalDate.class));
        s.setScore(rs.getDouble("score"));
        s.setTasksDone(rs.getInt("tasks_done"));
        s.setFocusMinutes(rs.getInt("focus_minutes"));
        s.setCollaborationCount(rs.getInt("collaboration_count"));
        s.setRawMetricsJson(rs.getString("raw_metrics_json"));
        s.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        s.setEmployeeName(rs.getString("employee_name"));
        return s;
    }
}
