package org.connected_sources.user;


import org.connected_sources.tenant.TenantContextHolder;
import org.connected_sources.tenant.fs.TenantAwareDataSourceManager;
import org.springframework.stereotype.Service;

@Service
public class OnboardingService {

  private final UserRepository userRepository;
  private final TenantContextHolder tenantContextHolder;
  private final TenantAwareDataSourceManager dataSourceManager;

  public OnboardingService(
          UserRepository userRepository,
          TenantContextHolder tenantContextHolder,
          TenantAwareDataSourceManager dataSourceManager
                          ) {
    this.userRepository = userRepository;
    this.tenantContextHolder = tenantContextHolder;
    this.dataSourceManager = dataSourceManager;
  }

  public void handleOnboarding(CreateUserAndProducerCommand command) {
    String producerId = userRepository.createProducerAndUser(command);
    tenantContextHolder.setTenantId("t__" + producerId);
    dataSourceManager.resolveDataSource();
  }
}
