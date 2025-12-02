package org.connected_sources.core.user.onboarding.repo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.connected_sources.core.user.onboarding.model.OnboardingRequestCmd;
import org.connected_sources.core.user.onboarding.model.OnboardingSummary;
import org.connected_sources.notification.core.Channel;
import org.connected_sources.notification.model.CuratorContact;
import org.connected_sources.shared.onboarding.OnboardingState;
import org.connected_sources.shared.tenantdb.DataSourceDescriptor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.connected_sources.shared.onboarding.OnboardingState.PREPARATION;

@Repository
@Transactional
public class OnboardingRepo {
  private final JdbcTemplate jdbc;
  private final ObjectMapper om;
  public OnboardingRepo(JdbcTemplate jdbc, ObjectMapper om) {
    this.jdbc = jdbc;
    this.om = om;
    System.out.println("OnboardingRepo created with jdbc: " + (jdbc != null));
    System.out.println("JdbcTemplate class: " + jdbc.getClass().getName());
    System.out.println("JdbcTemplate package: " + jdbc.getClass().getPackageName());
  }

    /* FUNCTION
    * createOrReuseRequest(cmd, correlationId)
    * ------------------------------------------------------
    * Idempotent insert of an onboarding_request in REQUESTED state.
    * If the idemKey exists, the previous row is returned instead of inserting.
    * Side effects:
    *  - writes idempotency_store (ttl)
    *  - adds onboarding_audit entry ("REQUESTED")
    *  - never triggers provisioning
    * Returns: request id
    */
    // WHY
    // We prefer ON CONFLICT DO NOTHING + SELECT existing over UPSERT-returning
    // to make the "audit first" behavior explicit on the happy path.
    // TEACHER
    // PostgreSQL snapshot rules: The INSERT + RETURNING happens in the same tx.
    // If RETURNING yields no row, we perform a second SELECT by natural key to
    // fetch the previous request. This two-step pattern is safe with READ COMMITTED
    // and avoids phantom inserts under concurrent submissions.
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

    public Optional<OnboardingSummary> findSummaryByNaturalKey(String producer, String email) {

        return jdbc.query("""
          SELECT id, state::text AS state, producer_name, email,
          website, vat_or_fiscal_code,correlation_id,created_at
          FROM onboarding_request 
          WHERE producer_name=? 
            and email =?
            order by id desc
            limit 1
          """,
                  ps -> {
                      ps.setString(1, producer);
                      ps.setString(2, email);
                  },
                  rs -> rs.next()
                          ? Optional.of(OnboardingSummary.fromRecord(rs))
                          : Optional.empty()
          );
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
//        catch (NullPointerException e) {
//            throw new RuntimeException("nessun valore tornato dalla funzione insert_if_expired", e);
//        }
        catch (Exception e) {
            throw new RuntimeException("fallita la query " + sql, e);
        }
    }

  public Optional<OnboardingSummary> findSummary(long id) {
    return jdbc.query("SELECT * from public.onboarding_get_request_by_id(?)",
                      ps -> ps.setInt(1, Math.toIntExact(id)),
                      rs -> rs.next()
                              ? Optional.of(OnboardingSummary.fromRecord(rs))
                              : Optional.empty()
                     );
  }

  public void transitionState(long reqId, OnboardingState newState, Long actorUserId, Map<String,Object> details) throws JsonProcessingException {
    jdbc.update("UPDATE onboarding_request SET state=?::onboarding_state, updated_at=now() WHERE id=?", newState.name(), reqId);
    audit(reqId, newState.name(), actorUserId, details);
  }

  public Optional<String> currentState(long reqId) {
    return Optional.ofNullable(jdbc.queryForObject("SELECT state::text FROM onboarding_request WHERE id=?",
                                                   String.class, reqId));
  }

/* FUNCTION
 * createTenant(tenantId, producerName, baseDir, sqlitePath)
 * ---------------------------------------------------------
 * Inserts tenant with state=PREPARATION and timestamps.
 * Uniqueness: tenant_id UNIQUE to prevent race conditions.
 * No side effects on FS/SQLite here: persistence only.
 */
  // WHY
  // We separate persistence from provisioning so failed FS/SQLite operations
  // cannot roll back the tenant row; compensations operate using this identity.
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


  public void setTenantState(String tenantId, OnboardingState newState) {
    jdbc.update("UPDATE tenant SET state=?::onboarding_state, updated_at=now() WHERE tenant_id=?", newState.name(), tenantId);
  }

    public List<CuratorContact> curators(Long requestId) {
        return jdbc.query("select * from public.onboarding_curators(?)",
                ps -> ps.setLong(1,requestId),
                (rs,_) -> {
                    String chanStr = rs.getString("channel");
                    Channel channel = Channel.valueOf(chanStr);
                    return new CuratorContact(
                            rs.getLong("user_id"),
                            channel, rs.getString("address")
                    );
                });
    }

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
}
