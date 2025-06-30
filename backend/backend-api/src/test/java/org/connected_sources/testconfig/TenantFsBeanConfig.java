package org.connected_sources.testconfig;

import org.connected_sources.tenant.TenantDatasourceRegistry;
import org.connected_sources.tenant.fs.FsTenantDatasourceRegistry;
import org.connected_sources.tenant.fs.FsTenantDatasourceResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class TenantFsBeanConfig {

  @Bean
  public TenantDatasourceRegistry tenantDatasourceRegistry() {
    Path basedir = Paths.get("/foo");
    FsTenantDatasourceResolver datasourceResolver = new FsTenantDatasourceResolver(basedir);
    return new FsTenantDatasourceRegistry(datasourceResolver);
  }
}
