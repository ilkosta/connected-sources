package org.connected_sources.shared.logging;

import java.time.Instant;
import java.util.Map;

/**
 * Immutable log record to be written to tenant FS/SQLite.
 * enriched with TenantContext.
 */
public record LogRecord(
        Instant ts,
        String tenantId,
        Long userId,
        String correlationId,
        TenantLogger.Category category,
        TenantLogger.Level level,
        String message,
        Map<String, Object> data
) {
  public LogRecord {
    if (ts == null) ts = Instant.now();
    if (level == null) level = TenantLogger.Level.INFO;
    if (category == null) category = TenantLogger.Category.DIAGNOSTIC;
  }
}
