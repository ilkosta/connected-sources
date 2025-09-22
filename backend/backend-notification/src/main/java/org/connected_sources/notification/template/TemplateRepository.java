package org.connected_sources.notification.template;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class TemplateRepository {

    private final JdbcTemplate jdbc;

    public TemplateRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<StoredTemplate> findByNameAndVersion(String name, Integer version) {
        String baseSql = "SELECT name, version, body_md FROM notification_template WHERE name = ? ";

        ResultSetExtractor<Optional<StoredTemplate>> extractor = rs ->
                rs.next() ? Optional.of(new StoredTemplate(
                        rs.getString("name"),
                        rs.getInt("version"),
                        rs.getString("body_md")
                )) : Optional.empty();

        if( version == null ) {
            return jdbc.query(baseSql + " order by id desc limit 1", extractor, name);
        }
        else {
            return jdbc.query(baseSql + " and version = ?", extractor, name,  version);
        }

    }

    public record StoredTemplate(String name, int version, String bodyMd) {}
}
