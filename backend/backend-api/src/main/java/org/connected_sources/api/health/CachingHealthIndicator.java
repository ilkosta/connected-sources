package org.connected_sources.api.health;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * Decorates a HealthIndicator with simple time-based caching.
 * If ttl == Duration.ZERO, caching is disabled.
 */
public class CachingHealthIndicator implements HealthIndicator {

  private final HealthIndicator delegate;
  private final Duration ttl;
  private final Clock clock;

  private static final class CacheEntry {
    final Health health;
    final Instant expiresAt;
    CacheEntry(Health h, Instant e) { this.health = h; this.expiresAt = e; }
  }

  private final AtomicReference<CacheEntry> cache = new AtomicReference<>();

  public CachingHealthIndicator(HealthIndicator delegate, Duration ttl) {
    this(delegate, ttl, Clock.systemUTC());
  }

  public CachingHealthIndicator(HealthIndicator delegate, Duration ttl, Clock clock) {
    this.delegate = Objects.requireNonNull(delegate);
    this.ttl = Objects.requireNonNull(ttl);
    this.clock = Objects.requireNonNull(clock);
  }

  @Override
  public Health health() {
    if (ttl.isZero() || ttl.isNegative()) {
      return delegate.health();
    }
    var now = Instant.now(clock);
    var entry = cache.get();
    if (entry != null && now.isBefore(entry.expiresAt)) {
      return entry.health;
    }
    // Recompute and publish
    synchronized (this) {
      // Double-check within lock
      entry = cache.get();
      if (entry != null && now.isBefore(entry.expiresAt)) {
        return entry.health;
      }
      Health h = delegate.health();
      var newEntry = new CacheEntry(h, now.plus(ttl));
      cache.set(newEntry);
      return h;
    }
  }
}