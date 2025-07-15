package org.connected_sources.api.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class OnboardingRequest {

  @NotBlank(message = "Producer name is required")
  private String producerName;

  @Email(message = "Valid email is required")
  @NotBlank(message = "Admin email is required")
  private String adminEmail;

  public OnboardingRequest() {
  }

  public String getProducerName() {
    return producerName;
  }

  public void setProducerName(String producerName) {
    this.producerName = producerName;
  }

  public String getAdminEmail() {
    return adminEmail;
  }

  public void setAdminEmail(String adminEmail) {
    this.adminEmail = adminEmail;
  }
}
