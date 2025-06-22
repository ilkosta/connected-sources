package org.connected_sources.shared;


public class Producer {
  private final String id;
  private final String name;
  private final String institutionalEmail;
  private final String legalHeadquarters;

  public Producer(String id, String name, String institutionalEmail, String legalHeadquarters) {
    this.id = id;
    this.name = name;
    this.institutionalEmail = institutionalEmail;
    this.legalHeadquarters = legalHeadquarters;
  }

  public String getId() { return id; }
  public String getName() { return name; }
  public String getInstitutionalEmail() { return institutionalEmail; }
  public String getLegalHeadquarters() {return legalHeadquarters;}
}