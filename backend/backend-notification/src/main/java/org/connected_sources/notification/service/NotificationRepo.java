package org.connected_sources.notification.service;
// src/main/java/com/acme/notification/service/NotificationRepo.java


import org.connected_sources.notification.core.Channel;
import org.connected_sources.shared.context.TenantContextHolder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Repository
public class NotificationRepo {

  private final JdbcTemplate jdbc;

  public NotificationRepo(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  /* =========================
     Idempotency (TTL window)
     ========================= */

  /** True if an idempotency key exists and is still valid (expires_at NULL or future). */
  public boolean existsIdem(String key) {
    Boolean exists = jdbc.queryForObject(
            "SELECT public.idem_exists(?)",
            Boolean.class, key);
    return Boolean.TRUE.equals(exists);
  }

  /** Insert idempotency key with optional TTL; NOOP on conflict. */
  public void putIdem(String key, Duration ttl) {
    jdbc.queryForObject(
            "select public.idem_put(?,?)",
            Boolean.class,
            key, ttl.getSeconds());
  }

  /* =========================
     Audit lifecycle
     ========================= */

  /**
   * Create a new audit row in PENDING status.
   * Stores redacted body if hasPii=true (at the moment a simple hash), otherwise full.
   * Returns the generated audit ULID/UUID.
   */
  public String createAudit(
          String eventType,
          String templateId,
          Channel channel,
          String subject,
          boolean hasPii,
          String bodyMd,
          String recipientKey) {
    String id = UUID.randomUUID().toString(); // TODO: swap to ULID or UUIDv7,v1

    String bodyAuditStored = hasPii ? "REDACTED_PARTIAL_HASH" : "FULL";
    String bodyAudit = hasPii ? sha256(bodyMd) : bodyMd;

    jdbc.update(
            "INSERT INTO notification_audit(" +
                    "id, correlation_id, tenant_id, user_id, template_id, channel, status, " +
                    "created_at, event_type, ttl, has_pii, body_audit_stored, body_audit,recipient_key " +
                    ") VALUES (?, ?, NULL, ?, NULL, ?::public.channel, 'PENDING', now(), ?, NULL, ?, ?, ?,?)",
            id,
            TenantContextHolder.get().correlationId(),
//            TenantContextHolder.get().tenantId(),
            TenantContextHolder.get().userId(),
//            templateId,
            channel.name(),
            eventType,
            hasPii,
            bodyAuditStored,
            bodyAudit,
            recipientKey
               );

    return id;
  }

  /** Update attempts/max_attempts on each try. */
  public void touchAttempt(String auditId, int attempt, int maxAttempts) {
    jdbc.update("UPDATE notification_audit SET attempts = ?, max_attempts = ? WHERE id = ?",
                attempt, maxAttempts, auditId);
  }

  /** Mark as SENT, store provider message id into provider_meta JSONB. */
  public void markSent(String auditId, String providerMessageId) {
    jdbc.update(
            "UPDATE notification_audit " +
                    "SET status = 'SENT', sent_at = now(), " +
                    "    provider_meta = COALESCE(provider_meta, '{}'::jsonb) || jsonb_build_object('providerId', ?) " +
                    "WHERE id = ?",
            providerMessageId, auditId);
  }

  /** Mark as FAILED or FAILED_PERMANENT and stamp error_at / error_code. */
  public void markFailed(String auditId, String errorCode, boolean permanent) {
    String status = permanent ? "FAILED_PERMANENT" : "FAILED";
    jdbc.update(
            "UPDATE notification_audit SET status = ?, error_code = ?, error_at = now() WHERE id = ?",
            status, errorCode, auditId);
  }

  /** Attach (or overwrite) a Redmine ticket id to the audit row. */
  public void attachTicket(String auditId, String ticketId) {
    Objects.requireNonNull(ticketId, "ticketId");
    jdbc.update("UPDATE notification_audit SET ticket_id = ? WHERE id = ?", ticketId, auditId);
  }

  /** Return the audit creation instant for TTL checks. */
  public Instant auditCreatedAt(String auditId) {
    return jdbc.queryForObject(
            "SELECT created_at FROM notification_audit WHERE id = ?",
            (rs, i) -> rs.getTimestamp(1).toInstant(),
            auditId);
  }

  /* =========================
     Helpers
     ========================= */

  private static String sha256(String s) {
    try {
      var md = java.security.MessageDigest.getInstance("SHA-256");
      byte[] dig = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      StringBuilder sb = new StringBuilder(dig.length * 2);
      for (byte b : dig) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (Exception e) {
      return "HASH_ERR";
    }
  }
}
