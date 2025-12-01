package org.connected_sources.core.user.async.onboarding;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.connected_sources.core.user.onboarding.model.ProvisioningSpec;
import org.connected_sources.notification.core.Channel;
import org.connected_sources.notification.events.EventType;
import org.connected_sources.notification.service.CuratorContact;
import org.connected_sources.notification.service.NotificationDispatcher;
import org.connected_sources.notification.service.NotificationRepo;
import org.connected_sources.notification.template.NotificationTemplate.*;
import org.connected_sources.shared.async.ContextAwareTaskDecorator;
import org.connected_sources.shared.logging.TenantLogger;
import org.connected_sources.shared.onboarding.OnboardingState;
import org.connected_sources.tenant.spi.TenantLifecycleManager;
import org.connected_sources.tenant.spi.db.TenantDbMigrator;
import org.connected_sources.core.user.onboarding.repo.OnboardingRepo;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.stream.Stream;

import static org.connected_sources.notification.template.NotificationTemplate.*;


/* DESIGN
 * -----------------
 * Asynchronous tenant provisioning pipeline:
 *   1) Plan FS layout and paths
 *   2) Create per-tenant directory tree
 *   3) Create SQLite DB file
 *   4) Run Flyway migrations + seeds
 *   5) Emit initial tenant logs
 * Deadline: 2 days (from conf) -> EXPIRED if unfinished.
 *
 * Why async: isolates heavy IO and retires independently of API latency.
 */
@Component
public class OnboardingProvisioner {
  private static String TENANT_PREFIX = "Tenant ";
  private final TaskExecutor exec;
  private final TaskScheduler scheduler;
  private final OnboardingRepo repo;
  private final TenantLifecycleManager fs;
  private final TenantDbMigrator migrator;
  private final NotificationDispatcher notifier;
  private final TenantLogger tlog;
  private final NotificationRepo notificationRepo;

  public OnboardingProvisioner(TaskExecutor exec,
                               TaskScheduler scheduler,
                               OnboardingRepo repo,
                               TenantLifecycleManager fs,
                               TenantDbMigrator dbMigrator,
                               NotificationDispatcher notifier,
                               TenantLogger tlog,
                               ContextAwareTaskDecorator ctxTaskDecorator, NotificationRepo notificationRepo) {
    this.exec = exec;
    if (exec instanceof ThreadPoolTaskExecutor te) {
      te.setTaskDecorator(ctxTaskDecorator);
    }
//    this.exec.setTaskDecorator(decorator); // propagate TenantContext/MDC
    this.scheduler = scheduler;
    this.repo = repo; this.fs = fs; this.migrator = dbMigrator;
    this.notifier = notifier; this.tlog = tlog;
    this.notificationRepo = notificationRepo;
  }

  public void enqueueProvisioning(long requestId, String tenantId, String baseDir, ProvisioningSpec in) {
    // deadline watchdog in 2 days
    scheduler.schedule(() -> onDeadline(requestId, tenantId),
                       Instant.now().plus(Duration.ofDays(2)));

    exec.execute(() -> {

      // CHECKLIST
      // Step order is intentional; do not reorder:
      //  - Paths resolved -> mkdirs -> SQLite file -> migrations -> logs -> state flip
      // This keeps partial artifacts minimal if we must compensate on failure.

      try {
        // 1) FS tree
        fs.provisionTenant(tenantId);
        Files.createDirectories(Path.of(baseDir, "content"));
        // 2) SQLite ds + migrate + seed
        DataSource ds = fs.sqlite(tenantId);
        migrator.migrate(ds, tenantId); // creates baseline, then apply per-tenant Flyway (seeds)
        // 3) Mark ENABLED
        repo.setTenantState(tenantId, OnboardingState.ENABLED);
        repo.transitionState(requestId, OnboardingState.ENABLED, null, Map.of("tenantId", tenantId));
        // 4) Notify requester & producer admin

        notifier.enqueue(ONBOARDING_ENABLED, in.producerAdminEmail(), Channel.EMAIL,
                         "Your workspace is ready", TENANT_PREFIX +tenantId+" enabled.", Duration.ofHours(24),
                         EventType.ONBOARDING_ENABLED, false);
      } catch (Exception e) {

        // GUIDE
        // If any step fails with a permanent error, mark FAILED and notify curator.
        // Transient errors schedule a backoff retry until the 2-day deadline.

        repo.setTenantState(tenantId, OnboardingState.FAILED);

        getCuratorsEmailStream(requestId)
                .forEach(
                c -> {
                  // open incident & notify curator
                  notifier.enqueue(ONBOARDING_FAILED, c.address(), Channel.EMAIL,
                          "Provisioning failed - " +tenantId, TENANT_PREFIX +tenantId+" error: "+e.getMessage(), Duration.ofHours(24),
                          EventType.ONBOARDING_FAILED, false);
                }
        );


        try {
          repo.transitionState(requestId, OnboardingState.FAILED, null, Map.of("err", e.getMessage()));
        } catch (JsonProcessingException ex) {
          throw new RuntimeException(ex);
        }

        // compensate: best-effort cleanup
        try { fs.deleteTenantArtifacts(tenantId); } catch(Exception _) {
          // better to do nothing for problem/incident management
        }

        // for debug...
          try {
              throw e;
          } catch (IOException ex) {
              throw new RuntimeException(ex);
          }
      }
    });
  }

  @NonNull
  private Stream<CuratorContact> getCuratorsEmailStream(long requestId) {
    return repo.curators(requestId).stream()
            .filter(c -> c.channel().equals(Channel.EMAIL));
  }

  private void onDeadline(long requestId, String tenantId) {
    // if not ENABLED/FAILED, mark EXPIRED and notify all parties
    repo.currentState(requestId).ifPresent(state -> {
      if (OnboardingState.PREPARATION.name().equals(state)) {
        repo.setTenantState(tenantId, OnboardingState.EXPIRED);
        try {
          repo.transitionState(requestId, OnboardingState.EXPIRED, null, Map.of("deadline", "2d"));
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }

        getCuratorsEmailStream(requestId)
                .forEach(
                c -> {
                  notifier.enqueue(ONBOARDING_EXPIRED, c.address(), Channel.EMAIL,
                          "Provisioning expired", TENANT_PREFIX +tenantId+" did not complete in time.", Duration.ofHours(24),
                          EventType.ONBOARDING_EXPIRED, false);
                }
        );
      }
    });
  }
}
