package org.connected_sources.notification.service;

public record DispatchOutcome(boolean isDuplicate, String auditId) {

  public static DispatchOutcome duplicate() {
    return new DispatchOutcome(true, null);
  }

  public static DispatchOutcome accepted(String id) {
    return new DispatchOutcome(false, id);
  }
}