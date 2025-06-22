package org.connected_sources.shared;

public interface NotificationService {
  void sendRegistrationEmail(String to, String producerName, String registrationLink);
}