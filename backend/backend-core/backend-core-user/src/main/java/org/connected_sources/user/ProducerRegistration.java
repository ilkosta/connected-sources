package org.connected_sources.user;

import java.time.Instant;

/**
 * oggetto transitorio, per mantenere temporanemamente
 * i dati di un produttore prima della conferma della rgistrazione
 * usato per validazine, confronto, temporizzazione
 * solo durante la fase di registrazione
 */
public class ProducerRegistration {

  private final String registrationId;
  private final String producerId;
  private final String name;
  private final String institutionalEmail;
  private final String legalHeadquarters;
  private final Instant requestedAt;

  public ProducerRegistration(String registrationId, String producerId, String name, String institutionalEmail,
                              String legalHeadquarters, Instant requestedAt) {
    this.registrationId = registrationId;
    this.producerId = producerId;
    this.name = name;
    this.institutionalEmail = institutionalEmail;
    this.legalHeadquarters = legalHeadquarters;
    this.requestedAt = requestedAt;
  }

  public boolean isExpired() {
    return requestedAt.plusSeconds(30L * 24 * 60 * 60).isBefore(Instant.now());
  }

  public String getRegistrationId() {
    return registrationId;
  }

  public String getProducerId() {
    return producerId;
  }

  public String getName() {
    return name;
  }

  public String getInstitutionalEmail() {
    return institutionalEmail;
  }

  public String getLegalHeadquarters() {
    return legalHeadquarters;
  }

  public Instant getRequestedAt() {
    return requestedAt;
  }
}
