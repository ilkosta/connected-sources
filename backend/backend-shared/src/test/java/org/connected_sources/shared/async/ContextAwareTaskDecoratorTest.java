package org.connected_sources.shared.async;

import org.connected_sources.shared.context.TenantContext;
import org.connected_sources.shared.context.TenantContextHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.connected_sources.shared.context.MDContext.*;

class ContextAwareTaskDecoratorTest {

  private final ContextAwareTaskDecorator decorator = new ContextAwareTaskDecorator();

  @AfterEach
  void cleanup() {
    TenantContextHolder.clear();
    MDC.clear();
  }

  @Test
  void propagatesContextAndMdcToChildAndCleansUp() {
    TenantContext parent = TenantContextHolder.from("t-1", 42L, "corr-abc");
    TenantContextHolder.set(parent);

    AtomicReference<TenantContext> seen = new AtomicReference<>();
    AtomicReference<String> mdcTenant = new AtomicReference<>();
    AtomicReference<String> mdcUser = new AtomicReference<>();
    AtomicReference<String> mdcCorr = new AtomicReference<>();

    Runnable child = decorator.decorate(() -> {
      seen.set(TenantContextHolder.get());
      mdcTenant.set(MDC.get(MDC_TENANT.getString()));
      mdcUser.set(MDC.get(MDC_USER.getString()));
      mdcCorr.set(MDC.get(MDC_CORR.getString()));
    });

    // a fake new thread
    child.run();

    assertThat(seen.get()).isNotNull();
    final TenantContext seenContext = seen.get();
    assertThat(seenContext.tenantId()).isEqualTo("t-1");
    assertThat(seenContext.userId()).isEqualTo(42L);
    assertThat(seenContext.correlationId()).isEqualTo("corr-abc");

    assertThat(mdcTenant.get()).isEqualTo("t-1");
    assertThat(mdcUser.get()).isEqualTo("42");
    assertThat(mdcCorr.get()).isEqualTo("corr-abc");

    // The decorator clears after run
    assertThat(MDC.get(MDC_TENANT.getString())).isNull();
    assertThat(TenantContextHolder.get()).isNull();
  }
}