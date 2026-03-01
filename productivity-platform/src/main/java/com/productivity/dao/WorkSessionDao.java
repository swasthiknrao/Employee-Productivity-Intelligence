package com.productivity.dao;

import com.productivity.config.DatabaseConfig;
import com.productivity.model.WorkSession;
import com.productivity.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WorkSessionDao {

    public int insert(WorkSession w) throws SQLException {
        String sql = "INSERT INTO work_sessions (employee_id, start_time, end_time, activity_type_id, app_name, notes) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, w.getEmployeeId());
            ps.setObject(2, w.getStartTime());
            ps.setObject(3, w.getEndTime());
            ps.setInt(4, w.getActivityTypeId());
            ps.setString(5, w.getAppName());
            ps.setString(6, w.getNotes());
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
            throw new SQLException("Insert work_session failed, no ID.");
        } finally {
            DBUtil.close(keys, ps, conn);
        }
    }

    public List<WorkSession> findByEmployeeIdAndDateRange(int employeeId, LocalDateTime from, LocalDateTime to) throws SQLException {
        String sql = "SELECT ws.id, ws.employee_id, ws.start_time, ws.end_time, ws.activity_type_id, ws.app_name, ws.notes, ws.created_at, at.name AS activity_type_name " +
                     "FROM work_sessions ws LEFT JOIN activity_types at ON ws.activity_type_id = at.id " +
                     "WHERE ws.employee_id = ? AND ws.start_time >= ? AND ws.start_time <= ? ORDER BY ws.start_time";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, employeeId);
            ps.setObject(2, from);
            ps.setObject(3, to);
            rs = ps.executeQuery();
            List<WorkSession> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    public List<WorkSession> findAllByEmployeeId(int employeeId, int limit) throws SQLException {
        String sql = "SELECT ws.id, ws.employee_id, ws.start_time, ws.end_time, ws.activity_type_id, ws.app_name, ws.notes, ws.created_at, at.name AS activity_type_name " +
                     "FROM work_sessions ws LEFT JOIN activity_types at ON ws.activity_type_id = at.id " +
                     "WHERE ws.employee_id = ? ORDER BY ws.start_time DESC LIMIT ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, employeeId);
            ps.setInt(2, limit);
            rs = ps.executeQuery();
            List<WorkSession> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    private static WorkSession mapRow(ResultSet rs) throws SQLException {
        WorkSession w = new WorkSession();
        w.setId(rs.getInt("id"));
        w.setEmployeeId(rs.getInt("employee_id"));
        w.setStartTime(rs.getObject("start_time", LocalDateTime.class));
        w.setEndTime(rs.getObject("end_time", LocalDateTime.class));
        w.setActivityTypeId(rs.getInt("activity_type_id"));
        w.setAppName(rs.getString("app_name"));
        w.setNotes(rs.getString("notes"));
        w.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        w.setActivityTypeName(rs.getString("activity_type_name"));
        return w;
    }
}
