package org.connected_sources.api.health;

import java.time.Duration;

//import org.connected_sources.tenant.spi.health.TenantStorageHealthIndicator;
import org.connected_sources.tenant.spi.health.TenantStorageHealthIndicator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class HealthConfig {

  @Value("${health.caching.ttl:30}") int caching_window_size;

  @Bean
  @Primary
  public HealthIndicator cachedFsSpaceHealthIndicator(TenantStorageHealthIndicator delegate) {

    return new CachingHealthIndicator(delegate, Duration.ofSeconds(caching_window_size));
  }
}