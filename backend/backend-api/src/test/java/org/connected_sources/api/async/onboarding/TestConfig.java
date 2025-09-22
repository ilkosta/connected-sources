package org.connected_sources.api.async.onboarding;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.connected_sources.core.user.onboarding.repo.OnboardingRepo;
//import org.connected_sources.shared.NotificationService;
import org.connected_sources.shared.async.ContextAwareTaskDecorator;
import org.connected_sources.shared.context.TenantContextHolder;
import org.connected_sources.tenant.spi.TenantLogLocator;
//import org.flywaydb.core.internal.jdbc.JdbcTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.mockito.Mockito.mock;

@Configuration
public class TestConfig {

    @Bean
    public ContextAwareTaskDecorator contextAwareTaskDecorator() {
        return new ContextAwareTaskDecorator();
    }

//    @Bean
////    @Primary
//    public NotificationService notificationService() {
//        return mock(NotificationService.class);
//    }

    @Bean
//    @Primary
    public TenantContextHolder tenantContextHolder() {
        return mock(TenantContextHolder.class);
    }

    @Bean
//    @Primary
    public TenantLogLocator tenantFsLocator() {
        return mock(TenantLogLocator.class);
    }

    @Bean
    public OnboardingRepo onboardingRepo(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        return new OnboardingRepo(jdbcTemplate, objectMapper);
    }
}