package org.connected_sources.user;

public class CreateUserAndProducerCommand {
  private final String producerName;
  private final String adminEmail;

  public CreateUserAndProducerCommand(String producerName, String adminEmail) {
    this.producerName = producerName;
    this.adminEmail = adminEmail;
  }

  public String getProducerName() {
    return producerName;
  }

  public String getAdminEmail() {
    return adminEmail;
  }
}
