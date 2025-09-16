package org.connected_sources.core.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public record User(
        Long user_id,
        String username,
        String email,
        String full_name,
        String status,
        Timestamp last_login_at,
        Timestamp last_failed_login_at,
        Timestamp created_at,
        boolean is_curator
) {
    public static User fromRecord(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("user_id"),
                rs.getString("username"),
                rs.getString ("email"),
                rs.getString ("full_name"),
                rs.getString ("status"),
                rs.getTimestamp("last_login_at"),
                rs.getTimestamp ("last_failed_login_at"),
                rs.getTimestamp ("created_at"),
                rs.getBoolean("is_curator")
        );
    }
}
