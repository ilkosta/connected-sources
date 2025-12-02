package org.connected_sources.notification.model;

import org.connected_sources.notification.core.Channel;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CuratorContactRowMapper implements RowMapper<CuratorContact> {
    @Override
    public CuratorContact mapRow(ResultSet rs, int rowNum) throws SQLException {
        Channel channel = Channel.valueOf(rs.getString("channel"));
        return new CuratorContact(
                rs.getLong("user_id"),
                channel,
                rs.getString("address")
        );
    }
}