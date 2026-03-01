package com.productivity.dao;

import com.productivity.config.DatabaseConfig;
import com.productivity.model.Communication;
import com.productivity.util.DBUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommunicationDao {

    public int insert(Communication c) throws SQLException {
        String sql = "INSERT INTO communications (from_employee_id, to_employee_id, channel, sent_at) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet keys = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, c.getFromEmployeeId());
            ps.setInt(2, c.getToEmployeeId());
            ps.setString(3, c.getChannel() != null ? c.getChannel() : "chat");
            ps.setObject(4, c.getSentAt());
            ps.executeUpdate();
            keys = ps.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
            throw new SQLException("Insert communication failed, no ID.");
        } finally {
            DBUtil.close(keys, ps, conn);
        }
    }

    public int countByEmployeeIdInPeriod(int employeeId, LocalDateTime from, LocalDateTime to) throws SQLException {
        String sql = "SELECT COUNT(*) FROM communications WHERE (from_employee_id = ? OR to_employee_id = ?) AND sent_at >= ? AND sent_at <= ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, employeeId);
            ps.setInt(2, employeeId);
            ps.setObject(3, from);
            ps.setObject(4, to);
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    public List<Communication> findAll(int limit) throws SQLException {
        String sql = "SELECT c.id, c.from_employee_id, c.to_employee_id, c.channel, c.sent_at, c.created_at, " +
                     "e1.name AS from_name, e2.name AS to_name FROM communications c " +
                     "LEFT JOIN employees e1 ON c.from_employee_id = e1.id LEFT JOIN employees e2 ON c.to_employee_id = e2.id " +
                     "ORDER BY c.sent_at DESC LIMIT ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, limit);
            rs = ps.executeQuery();
            List<Communication> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    private static Communication mapRow(ResultSet rs) throws SQLException {
        Communication c = new Communication();
        c.setId(rs.getInt("id"));
        c.setFromEmployeeId(rs.getInt("from_employee_id"));
        c.setToEmployeeId(rs.getInt("to_employee_id"));
        c.setChannel(rs.getString("channel"));
        c.setSentAt(rs.getObject("sent_at", LocalDateTime.class));
        c.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        c.setFromName(rs.getString("from_name"));
        c.setToName(rs.getString("to_name"));
        return c;
    }
}
