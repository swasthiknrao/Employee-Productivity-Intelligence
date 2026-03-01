package com.productivity.dao;

import com.productivity.config.DatabaseConfig;
import com.productivity.model.ActivityType;
import com.productivity.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityTypeDao {

    public List<ActivityType> findAll() throws SQLException {
        String sql = "SELECT id, name, is_focus_type FROM activity_types ORDER BY name";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConfig.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            List<ActivityType> list = new ArrayList<>();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    public ActivityType findById(int id) throws SQLException {
        String sql = "SELECT id, name, is_focus_type FROM activity_types WHERE id = ?";
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

    private static ActivityType mapRow(ResultSet rs) throws SQLException {
        ActivityType a = new ActivityType();
        a.setId(rs.getInt("id"));
        a.setName(rs.getString("name"));
        a.setFocusType(rs.getBoolean("is_focus_type"));
        return a;
    }
}
