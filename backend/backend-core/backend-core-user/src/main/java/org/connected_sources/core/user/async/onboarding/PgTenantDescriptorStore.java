package org.connected_sources.core.user.async.onboarding;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.connected_sources.shared.tenantdb.DataSourceDescriptor;
import org.connected_sources.shared.tenantdb.DbProvider;
import org.connected_sources.tenant.spi.db.TenantDescriptorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PgTenantDescriptorStore implements TenantDescriptorStore {
  private final JdbcTemplate jdbc;
  private final ObjectMapper om;

  public PgTenantDescriptorStore(JdbcTemplate jdbc, ObjectMapper om) {
    this.jdbc = jdbc; this.om = om;
  }

  @Override
  public DataSourceDescriptor readDescriptor(String tenantId) {
    return jdbc.queryForObject("""
        SELECT ds_provider, ds_config::text
        FROM tenant WHERE tenant_id=?
        """, (rs, rn) -> {
      var provider = DbProvider.valueOf(rs.getString(1));
      JsonNode node = null;
      try {
        node = om.readTree(rs.getString(2));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
      return new DataSourceDescriptor(
              provider,
              node.path("url").isMissingNode() ? null : node.path("url").asText(null),
              node.path("username").isMissingNode() ? null : node.path("username").asText(null),
              node.path("password").isMissingNode() ? null : node.path("password").asText(null),
              om.convertValue(node.path("pool"), java.util.Map.class)
      );
    }, tenantId);
  }
}
