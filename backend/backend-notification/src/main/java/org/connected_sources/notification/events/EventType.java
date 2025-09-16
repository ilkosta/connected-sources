package org.connected_sources.notification.events;

public enum EventType {
    ONBOARDING_REQUESTED,
    ONBOARDING_ACCEPTED,
    ONBOARDING_ENABLED,
    ONBOARDING_FAILED,
    ONBOARDING_EXPIRED;

    public static EventType fromString(String s) {
        return EventType.valueOf(s.trim().toUpperCase());
    }
}
