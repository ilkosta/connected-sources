package org.connected_sources.shared.context;

public enum MDContext {
  MDC_TENANT("tenantId"),
  MDC_USER("userId"),
  MDC_CORR("correlationId");

  private final String context;

  MDContext(String context) {
    this.context = context;
  }

  public String getString() {
    return context;
  }
}
