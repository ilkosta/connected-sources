package org.connected_sources.core.user.onboarding.repo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.connected_sources.core.user.onboarding.model.OnboardingRequestCmd;
import org.connected_sources.core.user.onboarding.model.OnboardingSummary;
import org.connected_sources.shared.tenantdb.DataSourceDescriptor;
import static org.connected_sources.shared.onboarding.OnboardingState.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Repository
@Transactional
public class OnboardingRepo {
  private final JdbcTemplate jdbc;
  private final ObjectMapper om;
  public OnboardingRepo(JdbcTemplate jdbc, ObjectMapper om) {
    this.jdbc = jdbc;
    this.om = om;
//    System.out.println("OnboardingRepo created with jdbc: " + (jdbc != null));
//    System.out.println("JdbcTemplate class: " + jdbc.getClass().getName());
//    System.out.println("JdbcTemplate package: " + jdbc.getClass().getPackageName());
  }

    /**
     * Attempts to "get" the idempotency key.
     * @return true if the key has just been inserted (first time); false if it already exists (duplicate)
     */
    public boolean tryClaimIdempotency(String key, Duration ttl) {
        Instant now = Instant.now();

        String sql = "select public.idem_put(?,?,?)";
        try {

            Boolean res = jdbc.queryForObject(sql, Boolean.class,
                    key, Timestamp.from(now), ttl.getSeconds());
            return res != null && res;
        }
        catch (NullPointerException e) {
            throw new RuntimeException("nessun valore tornato dalla funzione insert_if_expired", e);
        }
        catch (Exception e) {
            throw new RuntimeException("fallita la query " + sql, e);
        }
    }


  public String createTenant(String tenantId, String producerName, String baseDir, DataSourceDescriptor dsdesc) throws JsonProcessingException {
//    try {
      final String cfg = om.writeValueAsString(dsdesc);
      jdbc.update("""
    INSERT INTO tenant(tenant_id, producer_name, base_dir, ds_provider, ds_config, state)
      VALUES (?,?,?,?,?::jsonb,?::onboarding_state)
    ON CONFLICT (tenant_id) DO NOTHING /*RETURNING tenant_id*/;
  """, tenantId, producerName, baseDir, dsdesc.provider().name(), cfg, PREPARATION.name()
      );
      return tenantId;

    }
//    catch (Exception e) { throw new RuntimeException(e); }



  /* idempotency */
  public boolean existsIdem(String key) {
    Boolean b = jdbc.queryForObject(
            "select public.exists_idem(?)",
            Boolean.class, key);
    return Boolean.TRUE.equals(b);
  }
  public boolean putIdem(String key, Duration ttl) {
    return jdbc.queryForObject("select public.idem_put(?,?)", Boolean.class, key, ttl).booleanValue();
  }

  private void audit(long reqId, String action, Long actor, Map<String, Object> details) throws JsonProcessingException {
      final String detailsJson = details != null ? om.writeValueAsString(details) : "{}";
      jdbc.update("INSERT INTO onboarding_audit(onboarding_id, action, actor_user_id, details_json) VALUES (?,?,?,?::jsonb)",
              reqId,
              action,
              actor,
              detailsJson);
  }

    public long createOrReuseRequest(
            OnboardingRequestCmd cmd,
            String correlationId) throws JsonProcessingException {

        Long id = jdbc.query("""
    
                        INSERT INTO onboarding_request(requester_user_id, producer_name, email, website, vat_or_fiscal_code, state, correlation_id)
    VALUES (?,?,?,?,?,'REQUESTED',?)
    ON CONFLICT DO NOTHING
    RETURNING id
    """,
                ps -> {
                    ps.setLong(1, cmd.requesterUserId());
                    ps.setString(2, cmd.producerName());
                    ps.setString(3, cmd.email());
                    ps.setString(4, cmd.website());
                    ps.setString(5, cmd.vatOrFiscalCode());
                    ps.setString(6, correlationId);
                },
                rs -> rs.next() ? rs.getLong(1) : null
        );

        if (id == null) {
            id = jdbc.queryForObject(
                    """
        SELECT id FROM
                    onboarding_request
        WHERE producer_name=?
                    AND email=?
        ORDER BY
                    created_at DESC LIMIT 1
      """, Long.class, cmd.

                            producerName(), cmd.email());
        }

        audit(id, "REQUESTED", cmd.requesterUserId(), Map.of("correlationId", correlationId));
        return id;

    }

    public Optional<OnboardingSummary> findSummary(long id) {
        return jdbc.query("SELECT * from public.onboarding_get_request_by_id(?)",
                ps -> ps.setInt(1, Math.toIntExact(id)),
                rs -> rs.next()
                        ? Optional.of(OnboardingSummary.fromRecord(rs))
                        : Optional.empty()
        );
    }

}
