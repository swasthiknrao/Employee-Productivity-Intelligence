package com.productivity.dao;

import com.productivity.config.DatabaseConfig;
import com.productivity.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Audit log for privacy/compliance: log sensitive actions.
 */
public class AuditLogDao {

    public void log(String userRole, String action, String entityType, String entityId) throws SQLException {
        String sql = "INSERT INTO audit_log (user_role, action, entity_type, entity_id) VALUES (?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, userRole);
            ps.setString(2, action);
            ps.setString(3, entityType);
            ps.setString(4, entityId);
            ps.executeUpdate();
        } finally {
            DBUtil.close(ps, conn);
        }
    }
}
