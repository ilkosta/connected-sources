package org.connected_sources.tenant.logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.connected_sources.shared.context.TenantContextHolder;
import org.connected_sources.shared.logging.LogRecord;
import org.connected_sources.tenant.spi.TenantDatasourceRegistry;
import org.connected_sources.tenant.spi.TenantLogLocator;
import org.connected_sources.tenant.spi.TenantLifecycleManager;
import org.connected_sources.tenant.spi.TenantOpsLogger;
import org.connected_sources.tenant.spi.db.TenantDbMigrator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Structured, tenant-scoped logger:
 * - FS JSONL per tenant/day
 * - SQLite ops_log for SECURITY and AUDIT
 * - Context enrichment via TenantContextHolder
 */
@Component
public class FsTenantOpsLogger implements TenantOpsLogger {

  private static final EnumSet<Category> TO_SQLITE = EnumSet.of(Category.SECURITY, Category.AUDIT);

//  private final FsTenantLifecycleManager fs;
  private final ObjectMapper json;

  private final TenantDatasourceRegistry dsRegistry;
  private final TenantLogLocator fsLocator;
  private final TenantDbMigrator migrator;
  private final TenantLifecycleManager tmanager;


  public FsTenantOpsLogger(TenantDatasourceRegistry dsRegistry,
                           TenantDbMigrator migrator,
                           TenantLogLocator fsLocator,
                           TenantLifecycleManager tmanager,
                           ObjectMapper objectMapper) {
    this.dsRegistry = dsRegistry;
    this.migrator = migrator;
    this.fsLocator = fsLocator;
    this.tmanager = tmanager;
    this.json = objectMapper != null ? objectMapper : new ObjectMapper();
  }

  /* =========================================================
   * TenantLogger SPI
   * ========================================================= */
  @Override
  public void log(Category category, Level level, String message, Map<String, Object> data) {
    // Enrich with context
    var ctx = TenantContextHolder.get();
    var record = new LogRecord(
            Instant.now(),
            ctx != null ? ctx.tenantId() : null,
            ctx != null ? ctx.userId() : null,
            ctx != null ? ctx.correlationId() : null,
            category,
            level != null ? level : Level.INFO,
            message,
            data
    );
    routeAndWrite(record);
  }

  /* =========================================================
   * Routing
   * ========================================================= */
  private void routeAndWrite(LogRecord r) {

    // Always write FS JSONL
    // I prefer to have the log files written also for the `default` tenant
    // ( when a tenant has not yet been created)
    // so that I can monitor the `default` tenant's log directory:
    // if it changes it means that errors are occurring during provisioning
    writeFsJsonl(r);

    if(Objects.equals(r.tenantId(), "default"))
      return;

    // write to SQLite for SECURITY/AUDIT
    if (TO_SQLITE.contains(r.category())) {
      insertOpsLog(r);
    }
  }

  /* =========================================================
   * FS JSONL writer
   * ========================================================= */
  private void writeFsJsonl(LogRecord r) {
    String tenantId = safeTenantId(r.tenantId());
    try {
      Path dir = fsLocator.logsDir(tenantId);
      Path file = dir.resolve(dailyFileName());

      Files.createDirectories(dir);
      // Append one line JSON record
      try (BufferedWriter w = Files.newBufferedWriter(file,
                                                      StandardCharsets.UTF_8,
                                                      StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
        w.write(toFsJsonLine(r));
        w.newLine();
      }
    } catch (IOException e) {
      // last-chance: avoid throwing from logging; if needed, you can add a fallback or a metric
      System.err.printf("FS log write failed for tenant=%s file=%s: %s%n", tenantId, dailyFileName(), e.getMessage());
    }
  }

  private static String dailyFileName() {
    // yyyy-MM-dd.jsonl
    return LocalDate.now().toString() + ".jsonl";
  }

  private String toFsJsonLine(LogRecord r) {
    // Keep a compact JSONL for FS (no huge nesting)
    var map = Map.of(
            "ts", r.ts().toString(),
            "tenantId", r.tenantId(),
            "userId", r.userId(),
            "correlationId", r.correlationId(),
            "category", r.category().name(),
            "level", r.level().name(),
            "message", r.message(),
            "data", r.data() == null ? Map.of() : r.data()
                    );
    try {
      return json.writeValueAsString(map);
    } catch (JsonProcessingException e) {
      // Fallback: stringify message + minimal fields
      return "{\"ts\":\"" + r.ts() + "\",\"tenantId\":\"" + r.tenantId() + "\",\"level\":\"" + r.level().name()
              + "\",\"category\":\"" + r.category().name() + "\",\"message\":" + quote(r.message()) + "}";
    }
  }

  private static String quote(String s) {
    return s == null ? "null" : "\"" + s.replace("\"", "\\\"") + "\"";
  }

  /* =========================================================
   * SQLite writer
   * ========================================================= */
  private void insertOpsLog(LogRecord r) {
    String tenantId = safeTenantId(r.tenantId());
    DataSource ds = tmanager.sqlite(tenantId);

    String sql = """
        INSERT INTO ops_log
          (id, ts, tenant_id, user_id, correlation_id, category, level, message, data_json)
        VALUES
          (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    try (Connection c = ds.getConnection();
         PreparedStatement ps = c.prepareStatement(sql)) {
      ps.setString(1, Ulids.newUlid());
      ps.setObject(2, r.ts());
      ps.setString(3, r.tenantId());
      if (r.userId() == null) ps.setNull(4, Types.BIGINT); else ps.setLong(4, r.userId());
      ps.setString(5, r.correlationId());
      ps.setString(6, r.category().name());
      ps.setString(7, r.level().name());
      ps.setString(8, r.message());
      ps.setString(9, toJson(r.data()));
      ps.executeUpdate();
    } catch (SQLException e) {
      System.err.printf("SQLite ops_log insert failed for tenant=%s: %s%n", tenantId, e.getMessage());
    }
  }

  /* =========================================================
   * Helpers
   * ========================================================= */
  private String toJson(Map<String, Object> data) {
    try {
      return json.writeValueAsString(data == null ? Map.of() : data);
    } catch (JsonProcessingException e) {
      return "{}";
    }
  }

  private static String safeTenantId(String tenantId) {
    return tenantId == null || tenantId.isBlank() ? "_unknown_" : tenantId;
  }

  private static final class Ulids {
    private Ulids() {}
    static String newUlid() {
      return UUID.randomUUID().toString().replace("-", "");
    }
  }
}
