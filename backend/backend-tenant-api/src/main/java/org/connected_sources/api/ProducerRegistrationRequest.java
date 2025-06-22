package org.connected_sources.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ProducerRegistrationRequest {
//  @NotBlank <-- generato dal sistema
  private String producerId;

  @NotBlank
  private String name;

  @Email
  private String institutionalEmail;

  @NotBlank
  private String legalHeadquarters;

  // getters and setters
  public String getProducerId() { return producerId; }
  public void setProducerId(String producerId) { this.producerId = producerId; }

  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getInstitutionalEmail() { return institutionalEmail; }
  public void setInstitutionalEmail(String institutionalEmail) { this.institutionalEmail = institutionalEmail; }

  public String getLegalHeadquarters() {
    return legalHeadquarters;
  }

  public void setLegalHeadquarters(String legalHeadquarters) {
    this.legalHeadquarters = legalHeadquarters;
  }
}
