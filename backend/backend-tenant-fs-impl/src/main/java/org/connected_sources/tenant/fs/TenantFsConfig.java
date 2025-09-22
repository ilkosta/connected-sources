package org.connected_sources.tenant.fs;

import org.connected_sources.tenant.spi.TenantDatasourceRegistry;
import org.connected_sources.tenant.spi.TenantDatasourceResolver;
import org.connected_sources.tenant.spi.TenantLifecycleManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration
@ConditionalOnProperty(name = "tenant.storage.type", havingValue = "fs")
public class TenantFsConfig {
//  @Bean
//  public FsTenantLifecycleManager tenantLifecycleManager(
//          FsTenantDatasourceResolver datasourceResolver,
//          FsTenantDatasourceRegistry registry,
//          @Value("${tenant.base-directory:./tenants}")
//            String rootDir ) {
//    return new FsTenantLifecycleManager(datasourceResolver, registry, rootDir);
//  }

  @Bean
  @ConditionalOnMissingBean
  public TenantLifecycleManager tenantLifecycleManager(
          TenantDatasourceResolver datasourceResolver,
          TenantDatasourceRegistry registry,
          @Value("${tenant.base-directory}") String rootDir) {
    return new FsTenantLifecycleManager(datasourceResolver, registry, rootDir);
  }

  @Bean
  public PathResolver pathResolver(
          @Value("${tenant.base-directory}") String baseDir) {
    return new PathResolver(Path.of(baseDir));
  }
}
