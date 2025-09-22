//package org.connected_sources.tenant.logger;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.connected_sources.tenant.spi.TenantDatasourceRegistry;
//import org.connected_sources.tenant.spi.TenantFsLocator; // if present
//import org.connected_sources.tenant.spi.TenantLifecycleManager;
//import org.connected_sources.tenant.spi.TenantOpsLogger;
//import org.connected_sources.tenant.spi.db.TenantDbMigrator;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class LoggingFsConfig {
//
//  @Bean
//  public TenantOpsLogger tenantOpsLogger(TenantDatasourceRegistry dsRegistry,
//                                         TenantDbMigrator migrator,
//                                         TenantFsLocator fsLocator,
//                                         TenantLifecycleManager tmanager,
//                                         ObjectMapper objmapper) {
//    return new FsTenantOpsLogger(
//            dsRegistry, migrator, fsLocator,tmanager,objmapper);
//  }
//}