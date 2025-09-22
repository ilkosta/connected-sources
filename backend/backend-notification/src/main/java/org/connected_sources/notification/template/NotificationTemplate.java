package org.connected_sources.notification.template;

public enum NotificationTemplate {
  // --- onboarding ---
  ONBOARDING_REQUESTED("onboarding-request"),
  ONBOARDING_APPROVED("onboarding-approve"),
  ONBOARDING_ENABLED("onboarding-enabled"),
  ONBOARDING_FAILED("onboarding-failed"),
  ONBOARDING_EXPIRED("onboarding-expired");

  private final String id;

  NotificationTemplate(String id) {
    this.id = id;
  }

  public String id() {
    return id;
  }

  public static NotificationTemplate fromId(String id) {
    for (NotificationTemplate t : values()) {
      if (t.id.equals(id)) {
        return t;
      }
    }
    throw new IllegalArgumentException("Unknown template id: " + id);
  }

}
