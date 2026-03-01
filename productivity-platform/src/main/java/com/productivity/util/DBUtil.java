package com.productivity.util;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * JDBC resource helpers: get connection from config and close resources safely.
 */
public final class DBUtil {

    private DBUtil() {}

    public static void close(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // log and ignore
            }
        }
    }

    public static void close(Statement st) {
        if (st != null) {
            try {
                st.close();
            } catch (SQLException e) {
                // log and ignore
            }
        }
    }

    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                // log and ignore
            }
        }
    }

    public static void close(ResultSet rs, Statement st, Connection conn) {
        close(rs);
        close(st);
        close(conn);
    }

    public static void close(Statement st, Connection conn) {
        close(st);
        close(conn);
    }

    public static void closeQuietly(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Exception ignored) {
            }
        }
    }
}
