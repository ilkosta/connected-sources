package org.connected_sources.api.async.onboarding;

//import org.connected_sources.api.ProducerRegistrationController;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.connected_sources.api.dto.onboarding.RegistrationPayload;
import org.connected_sources.api.web.onboarding.OnboardingController;
import org.connected_sources.core.user.async.onboarding.OnboardingProvisioner;
import org.connected_sources.core.user.onboarding.model.ProvisioningSpec;
import org.connected_sources.core.user.onboarding.repo.OnboardingRepo;
import org.connected_sources.notification.core.Channel;
import org.connected_sources.notification.events.EventType;
import org.connected_sources.notification.service.NotificationDispatcher;
import org.connected_sources.notification.service.NotificationRepo;
import org.connected_sources.notification.template.NotificationTemplate;
import org.connected_sources.shared.async.ContextAwareTaskDecorator;
import org.connected_sources.shared.context.TenantContext;
import org.connected_sources.shared.context.TenantContextHolder;
import org.connected_sources.shared.onboarding.OnboardingState;
import org.connected_sources.tenant.fs.FsTenantLifecycleManager;
import org.connected_sources.tenant.spi.TenantLifecycleManager;
import org.connected_sources.tenant.spi.TenantOpsLogger;
import org.connected_sources.tenant.spi.db.TenantDbMigrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.mockito.Mockito.*;

class OnboardingProvisionerTest {

    @TestConfiguration
    static class TestBeans {
        @Bean
        @Primary
        TenantLifecycleManager tenantLifecycleManagerMock() {
            return Mockito.mock(TenantLifecycleManager.class);
        }
    }

    // Deps
    ThreadPoolTaskExecutor exec;
    CapturingScheduler scheduler;
    OnboardingRepo repo;
    FsTenantLifecycleManager fs;
    TenantDbMigrator migrator;
    NotificationDispatcher notifier;
    NotificationRepo notificationRepo;
    TenantOpsLogger log;
    ContextAwareTaskDecorator decorator;

    OnboardingProvisioner provisioner;

    @Value("${tenant.base-directory}")
    String basedir;

    @BeforeEach
    void setup() {
        // Synchronous executor for deterministic behavior
        exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(1);
        exec.setMaxPoolSize(1);
        exec.setTaskDecorator(r -> r); // no-op, we set context directly
        exec.afterPropertiesSet();

        scheduler = new CapturingScheduler();
        repo = mock(OnboardingRepo.class);
        fs = mock(FsTenantLifecycleManager.class);
        migrator = mock(TenantDbMigrator.class);
        notifier = mock(NotificationDispatcher.class);
        log = mock(TenantOpsLogger.class);
        notificationRepo = mock(NotificationRepo.class);

        decorator = new ContextAwareTaskDecorator() { //stub
            @Override
            public Runnable decorate(Runnable runnable) {
                return runnable; // no-op
            }
        };

        // -----
        // only for tests: use a direct/synchronous executor for the async threds...
        Executor direct = Runnable::run;
        ThreadPoolTaskExecutor inline = new ThreadPoolTaskExecutor() {
            @Override public void execute(Runnable task) { task.run(); } // sync
        };
        inline.afterPropertiesSet();
        provisioner = new OnboardingProvisioner(inline, scheduler, repo, fs, migrator, notifier, log, decorator, notificationRepo);
        // -----
        TenantContextHolder.set(new TenantContext("t-provision", 7L, "corr-prov"));

    }

    @Test
    void success_provisions_enables_and_notifies() throws Exception {
        when(fs.sqlite("pippo")).thenReturn(mock(DataSource.class));

        RegistrationPayload payload = new RegistrationPayload("admin@pippo.test", "pippo SRL", List.of(7L));
        ProvisioningSpec pspec = new ProvisioningSpec(payload.producerAdminEmail(), payload.initialUsers());
        provisioner.enqueueProvisioning(101L, "pippo",
                basedir + "/pippo",
                pspec);

        // deadline scheduled
        assert scheduler.tasks.size() == 1;
        // FS + migrate
        verify(fs).provisionTenant("pippo");
//        verify(migrator).migrate(any(DataSource.class));
        // state transitions & notifications
        verify(repo).setTenantState("pippo", OnboardingState.ENABLED);
        verify(repo).transitionState(101L, OnboardingState.ENABLED, null, Map.of("tenantId","pippo"));
        verify(notifier).enqueue(eq(NotificationTemplate.ONBOARDING_ENABLED), eq("admin@pippo.test"), eq(Channel.EMAIL),
                contains("ready"), contains("pippo"), any(), eq(EventType.ONBOARDING_ENABLED), eq(false));
    }

//    @Test
//    void failure_marksFailed_notifiesCurator() throws Exception {
//        // cause provisionTenant to throw
//        doThrow(new RuntimeException("disk full")).when(fs).provisionTenant("pippo");
//
//        RegistrationPayload payload = new RegistrationPayload("admin@pippo.test", "pippo SRL", List.of(7L));
//        ProvisioningSpec pspec = new ProvisioningSpec(payload.producerAdminEmail(), payload.initialUsers());
//        provisioner.enqueueProvisioning(101L, "pippo", basedir, pspec);
//
//        verify(repo).setTenantState("pippo", OnboardingState.FAILED);
//        verify(repo).transitionState(eq(101L), eq(OnboardingState.FAILED), isNull(), argThat(m -> String.valueOf(m.get("err")).contains("disk full")));
//        verify(notifier).enqueue(eq(NotificationTemplate.ONBOARDING_FAILED), anyString(), eq(Channel.EMAIL),
//                contains("failed"), contains("pippo"), any(), eq(EventType.ONBOARDING_FAILED), eq(false));
////        verify(fs).deleteTenantArtifacts("pippo");
//    }

//    @Test
//    void deadline_marksExpired_whenStillPreparation() throws JsonProcessingException {
//        when(repo.currentState(101L)).thenReturn(java.util.Optional.of("PREPARATION"));
//
//        RegistrationPayload payload = new RegistrationPayload("admin@pippo.test", "pippo SRL", List.of(7L));
//        ProvisioningSpec pspec = new ProvisioningSpec(payload.producerAdminEmail(), payload.initialUsers());
//        provisioner.enqueueProvisioning(101L, "pippo", basedir + "/pippo", pspec);
//
//        // fire the scheduled deadline task immediately
//        scheduler.tasks.get(0).runnable.run();
//
//        verify(repo).setTenantState("pippo", OnboardingState.EXPIRED);
//        verify(repo).transitionState(eq(101L), eq(OnboardingState.EXPIRED), isNull(), argThat(m -> "2d".equals(m.get("deadline"))));
//        verify(notifier).enqueue(eq(NotificationTemplate.ONBOARDING_EXPIRED), anyString(), eq(Channel.EMAIL),
//                contains("expired"), contains("pippo"), any(), eq(EventType.ONBOARDING_EXPIRED), eq(false));
//    }

    /** minimal in-test scheduler that captures one-shot tasks */
    static class CapturingScheduler implements TaskScheduler {
        static class Task { final Runnable runnable; final Instant when; Task(Runnable r, Instant w){this.runnable=r;this.when=w;} }
        java.util.List<Task> tasks = new java.util.ArrayList<>();
        @Override public java.util.concurrent.ScheduledFuture<?> schedule(Runnable task, Instant startTime) {
            tasks.add(new Task(task, startTime)); return null;
        }
        @Override public java.util.concurrent.ScheduledFuture<?> schedule(Runnable task, org.springframework.scheduling.Trigger trigger) { return null; }
        @Override public java.util.concurrent.ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Instant startTime, Duration period) { return null; }
        @Override public java.util.concurrent.ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Duration period) { return null; }
        @Override public java.util.concurrent.ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Instant startTime, Duration delay) { return null; }
        @Override public java.util.concurrent.ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Duration delay) { return null; }
    }
}
