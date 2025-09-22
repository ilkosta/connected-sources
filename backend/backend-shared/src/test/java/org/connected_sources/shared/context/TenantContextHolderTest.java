package org.connected_sources.shared.context;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThat;
import static org.connected_sources.shared.context.MDContext.*;

class TenantContextHolderTest {

  @AfterEach
  void tearDown() {
    TenantContextHolder.clear();
    MDC.clear();
  }

  @Test
  void setAndGetContextShouldWork() {
    TenantContext ctx = TenantContextHolder.from("tenantA", 42L, "corr-123");
    TenantContextHolder.set(ctx);

    TenantContext current = TenantContextHolder.get();

    assertThat(current.tenantId()).isEqualTo("tenantA");
    assertThat(current.userId()).isEqualTo(42L);
    assertThat(current.correlationId()).isEqualTo("corr-123");
  }

  @Test
  void shouldPopulateMdcWhenSet() {
    TenantContext ctx = TenantContextHolder.from("tenantB", 100L, "corr-xyz");
    TenantContextHolder.set(ctx);

    assertThat(MDC.get(MDC_TENANT.getString())).isEqualTo("tenantB");
    assertThat(MDC.get(MDC_USER.getString())).isEqualTo("100");
    assertThat(MDC.get(MDC_CORR.getString())).isEqualTo("corr-xyz");
  }

  @Test
  void clearShouldRemoveContextAndMdc() {
    TenantContextHolder.set(TenantContextHolder.from("t", 1L, "c"));
    TenantContextHolder.clear();

    assertThat(TenantContextHolder.get()).isNull();
    assertThat(MDC.get(MDC_TENANT.getString())).isNull();
    assertThat(MDC.get(MDC_USER.getString())).isNull();
    assertThat(MDC.get(MDC_CORR.getString())).isNull();
  }

  @Test
  void shouldGenerateCorrelationIdIfMissing() {
    TenantContext ctx = TenantContextHolder.from("tenantX", null, null);

    assertThat(ctx.correlationId()).isNotBlank();
  }
}
