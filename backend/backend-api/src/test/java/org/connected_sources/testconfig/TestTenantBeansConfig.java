package org.connected_sources.testconfig;

import org.connected_sources.tenant.TenantContextHolder;
import org.connected_sources.tenant.TenantDatasourceRegistry;
import org.connected_sources.tenant.fs.FsTenantDatasourceRegistry;

import org.connected_sources.tenant.fs.FsTenantDatasourceResolver;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import java.nio.file.Path;
import java.nio.file.Paths;

@TestConfiguration
public class TestTenantBeansConfig {

  @Bean
  public TenantContextHolder tenantContextHolder() {
    return new TenantContextHolder();
  }

  @Bean
  public TenantDatasourceRegistry tenantDatasourceRegistry() {
    Path basedir = Paths.get("/foo");
    FsTenantDatasourceResolver datasourceResolver = new FsTenantDatasourceResolver(basedir);
    return new FsTenantDatasourceRegistry(datasourceResolver); // adjust as needed
  }
}
