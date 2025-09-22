//package org.connected_sources.core.user;
//
//
//import org.connected_sources.shared.context.TenantContextHolder;
//import org.connected_sources.tenant.spi.TenantAwareDataSourceManager;
//import org.springframework.stereotype.Service;
//
//@Service
//public class OnboardingService {
//
//  private final UserRepository userRepository;
//  private final TenantContextHolder tenantContextHolder;
//  private final TenantAwareDataSourceManager dataSourceManager;
//
//  public OnboardingService(
//          UserRepository userRepository,
//          TenantContextHolder tenantContextHolder,
//          TenantAwareDataSourceManager dataSourceManager
//                          ) {
//    this.userRepository = userRepository;
//    this.tenantContextHolder = tenantContextHolder;
//    this.dataSourceManager = dataSourceManager;
//  }
//
//  public void handleOnboarding(CreateUserAndProducerCommand command) {
//    throw new RuntimeException();
////    String producerId = userRepository.createProducerAndUser(command);
////    tenantContextHolder.setTenantId("t__" + producerId);
////    dataSourceManager.resolveDataSource();
//  }
//}
