package org.connected_sources.notification.service;

import org.connected_sources.notification.core.Channel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcContactInformationRepo implements ContactInformationRepo {

    private final JdbcTemplate jdbc;

    public JdbcContactInformationRepo(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<String> findPrimaryEmail(long userId) {
        // Picks the first enabled EMAIL contact by ascending priority.
        // Extracts value->>'address' or value->>'email' as a fallback.
        String sql = """
        SELECT COALESCE(value->>'address', value->>'email') AS email
        FROM contact_information
        WHERE user_id = ? AND channel = 'EMAIL' AND enabled = TRUE
        ORDER BY priority ASC
        LIMIT 1
        """;
        return jdbc.query(sql, rs -> rs.next()
                ? Optional.ofNullable(rs.getString("email"))
                : Optional.empty(), userId);
    }

    @Override
    public List<CuratorContact> curatorsPrimaryEmail() {
        return jdbc.query("select * from public.curators_address('EMAIL'::channel)",
                (rs,_) -> {
                    String chanStr = rs.getString("channel");
                    Channel channel = Channel.valueOf(chanStr);
                    return new CuratorContact(
                            rs.getLong("user_id"),
                            channel, rs.getString("address")
                            );
                });
    }
}

