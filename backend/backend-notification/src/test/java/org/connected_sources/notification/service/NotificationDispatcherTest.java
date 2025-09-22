package org.connected_sources.notification.service;

import org.connected_sources.notification.core.Channel;
import org.connected_sources.notification.core.ChannelAdapter;
import org.connected_sources.notification.core.ChannelAdapterFactory;
import org.connected_sources.notification.events.EventType;
import org.connected_sources.notification.incident.DelegatingCategoryResolver;
import org.connected_sources.notification.incident.RedmineTicketLocator;
import org.connected_sources.notification.template.NotificationTemplate;
import org.connected_sources.shared.context.TenantContextHolder;
import org.connected_sources.shared.logging.TenantLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Tests the dispatcher behaviors:
 * - idempotency duplicate
 * - retry scheduling (transient & permanent)
 * - TTL finite vs infinite
 * - Redmine open-ticket reuse and creation paths
 */
public class NotificationDispatcherTest {

  private NotificationRepo repo;
  private DelegatingCategoryResolver categoryResolver;
  private RedmineTicketLocator ticketLocator;
  private CapturingScheduler scheduler;
  private NotificationDispatcher dispatcher;

  // Per-channel stubs used by the real factory
  private StubChannelAdapter emailStub;
  private StubChannelAdapter telegramStub;

  private TenantLogger logger;

  @BeforeEach
  void setUp() {
    repo = mock(NotificationRepo.class);
    categoryResolver = mock(DelegatingCategoryResolver.class);
    ticketLocator = mock(RedmineTicketLocator.class);
    scheduler = new CapturingScheduler();

    emailStub = new StubChannelAdapter(Channel.EMAIL);
    telegramStub = new StubChannelAdapter(Channel.TELEGRAM);

    TenantContextHolder.set(TenantContextHolder.from("t1", 1L, "corr-xyz"));

    ChannelAdapterFactory factory = new ChannelAdapterFactory(List.of(
            (ChannelAdapter) emailStub,
            (ChannelAdapter) telegramStub
                                                                     ));
    logger = mock(TenantLogger.class);

    // Build dispatcher with explicit constructor (all deps and @Values)
    dispatcher = new NotificationDispatcher(
            categoryResolver,
            ticketLocator,
            /* projectId */ 123,
            /* trackerId */ 7,
            factory,
            repo,
            scheduler,
            logger,
            /* maxAttempts */ 3,
            /* initialBackoff */ Duration.ofSeconds(1),
            /* backoffMult */ 2.0,
            /* maxBackoff */ Duration.ofSeconds(10)
    );
  }

  @AfterEach
  void tearDown() { TenantContextHolder.clear(); }

  // ---------- Idempotency duplicate ----------
  @Test
  void enqueue_duplicateWithinTtl_returnsDuplicate() {
    when(repo.existsIdem(NotificationTemplate.ONBOARDING_ENABLED.id() + "|user@x|corr-xyz")).thenReturn(true);

    var out = dispatcher.enqueue(
            NotificationTemplate.ONBOARDING_ENABLED, "user@x", Channel.EMAIL, "subject", "body",
            Duration.ofHours(24), EventType.ONBOARDING_ENABLED, false //, "corr-1"
                                );

    assertThat(out.isDuplicate()).isTrue();
    verify(repo, never()).createAudit(
            anyString(),
            eq(NotificationTemplate.ONBOARDING_ENABLED.id()),
            any(Channel.class),
            anyString(),
            anyBoolean(),
            anyString());
  }

  // ---------- Retry scheduling: transient then permanent ----------
  @Test
  void retrySchedules_forTransientAndPermanent() {
    // New audit id flow
    when(repo.existsIdem(anyString())).thenReturn(false);
    when(repo.createAudit(anyString(), anyString(),any(Channel.class), anyString(), anyBoolean(), anyString())).thenReturn("aid-1");

    // First attempt: transient failure
    emailStub.setMode(StubChannelAdapter.Mode.TRANSIENT_FAIL);
    emailStub.setErrorCode("SMTP_421");

    dispatcher.enqueue(NotificationTemplate.ONBOARDING_ENABLED, "user@x", Channel.EMAIL, "s", "b",
                       Duration.ofHours(24), EventType.ONBOARDING_ENABLED, false //, "corr-2"
                      );

    // Expect 1 scheduled task for retry
    assertThat(scheduler.tasks).hasSize(1);

    // Second attempt: switch to permanent failure,
    // ensure ticket flow triggers
    // schedules again (until exhausted)
    emailStub.setMode(StubChannelAdapter.Mode.PERMANENT_FAIL);
    emailStub.setErrorCode("SMTP_550");

    when(categoryResolver.resolve("SMTP_550")).thenReturn(Optional.of("Mail Delivery"));
    when(ticketLocator.findOpenTicket(123, 7, "Mail Delivery")).thenReturn(Optional.of("TICK-77"));

    scheduler.tasks.get(0).runnable.run();

    // attach existing ticket, and schedule another attempt (since attempts not yet exhausted)
    verify(ticketLocator, never()).createTicket(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
    verify(repo).attachTicket("aid-1", "TICK-77");
    assertThat(scheduler.tasks.size()).isGreaterThanOrEqualTo(2);
  }

  // ---------- TTL finite expires -> mark FAILED ----------
  @Test
  void ttlFinite_expiresAndMarksFailed() {
    when(repo.existsIdem(anyString())).thenReturn(false);
    when(repo.createAudit(anyString(), anyString(),any(Channel.class), anyString(), anyBoolean(), anyString())).thenReturn("aid-2");
    // Created 25h ago; with TTL 24h, should expire on retry
    when(repo.auditCreatedAt("aid-2")).thenReturn(Instant.now().minus(Duration.ofHours(25)));

    emailStub.setMode(StubChannelAdapter.Mode.TRANSIENT_FAIL);
    dispatcher.enqueue(NotificationTemplate.ONBOARDING_ENABLED, "user@x", Channel.EMAIL, "s", "b",
                       Duration.ofHours(24), EventType.ONBOARDING_ENABLED, false //, "corr-3"
                      );

    // Run the scheduled retry
    scheduler.tasks.get(0).runnable.run();

    verify(repo).markFailed(eq("aid-2"), anyString(), eq(false));
  }

  // ---------- TTL infinite (Duration.ZERO) -> never blocked by TTL ----------
  @Test
  void ttlInfinite_neverBlockedByTtl() {
    when(repo.existsIdem(anyString())).thenReturn(false);
    when(repo.createAudit(anyString(), anyString(),any(Channel.class), anyString(), anyBoolean(), anyString())).thenReturn("aid-3");
    // Even if very old, Duration.ZERO means infinite TTL
    when(repo.auditCreatedAt("aid-3")).thenReturn(Instant.now().minus(Duration.ofDays(3650)));

    emailStub.setMode(StubChannelAdapter.Mode.TRANSIENT_FAIL);
    dispatcher.enqueue(NotificationTemplate.ONBOARDING_ENABLED, "user@x", Channel.EMAIL, "s", "b",
                       Duration.ZERO, EventType.ONBOARDING_ENABLED, false //, "corr-4"
                      );

    scheduler.tasks.get(0).runnable.run();

    // Must NOT markFailed due to TTL
    verify(repo, never()).markFailed(eq("aid-3"), anyString(), anyBoolean());
  }

  // ---------- Redmine: open ticket exists -> reuse ----------
  @Test
  void permanentError_reusesOpenTicketAndPersists() {
    when(repo.existsIdem(anyString())).thenReturn(false);
    when(repo.createAudit(anyString(), anyString(),any(Channel.class), anyString(), anyBoolean(), anyString())).thenReturn("aid-4");

    emailStub.setMode(StubChannelAdapter.Mode.PERMANENT_FAIL);
    emailStub.setErrorCode("SMTP_550");

    when(categoryResolver.resolve("SMTP_550")).thenReturn(Optional.of("Mail Delivery"));
    when(ticketLocator.findOpenTicket(123, 7, "Mail Delivery")).thenReturn(Optional.of("TICK-99"));

    dispatcher.enqueue(NotificationTemplate.ONBOARDING_REQUESTED, "user@x", Channel.EMAIL, "s", "b",
                       Duration.ofHours(24), EventType.ONBOARDING_REQUESTED, false //, "corr-5"
                       );

    scheduler.tasks.get(0).runnable.run();

    verify(ticketLocator, never()).createTicket(anyInt(), anyInt(), anyString(), anyString(), anyString(), anyString());
    verify(repo).attachTicket("aid-4", "TICK-99");
  }

  // ---------- Redmine: none open -> create and persist ----------
  @Test
  void permanentError_createsTicketWhenNoneOpen() {
    when(repo.existsIdem(anyString())).thenReturn(false);
    when(repo.createAudit(anyString(), anyString(),any(Channel.class), anyString(), anyBoolean(), anyString())).thenReturn("aid-5");

    emailStub.setMode(StubChannelAdapter.Mode.PERMANENT_FAIL);
    emailStub.setErrorCode("SMTP_550");

    when(categoryResolver.resolve("SMTP_550")).thenReturn(Optional.of("Mail Delivery"));
    when(ticketLocator.findOpenTicket(123, 7, "Mail Delivery")).thenReturn(Optional.empty());
    when(ticketLocator.createTicket(eq(123), eq(7), eq("Mail Delivery"),
                                    anyString(), anyString(), anyString())).thenReturn("TICK-100");

    dispatcher.enqueue(NotificationTemplate.ONBOARDING_REQUESTED, "user@x", Channel.EMAIL, "s", "b",
                       Duration.ofHours(24), EventType.ONBOARDING_REQUESTED, false //, "corr-6"
                       );

    scheduler.tasks.get(0).runnable.run();

    verify(ticketLocator).createTicket(eq(123), eq(7), eq("Mail Delivery"),
                                       anyString(), anyString(), anyString());
    verify(repo).attachTicket("aid-5", "TICK-100");
  }
}
