//package org.connected_sources.shared;
//
//import java.util.Objects;
//import java.util.Set;
//
//import jakarta.persistence.*;
//
//@Entity
//@Inheritance(strategy = InheritanceType.JOINED)
//public class User {
//  @Id
//  @GeneratedValue
//  private Long id;
//
//  private String firstname;
//  private String lastname;
//  private char status;
//  private String email;
//  private String gitUsername;
//
//  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
//  private Set<ContactInformations> contactInformations;
//
//  public Long getId() {
//    return id;
//  }
//
//  public void setId(Long id) {
//    this.id = id;
//  }
//
//  public String getFirstname() {
//    return firstname;
//  }
//
//  public void setFirstname(String firstname) {
//    this.firstname = firstname;
//  }
//
//  public String getLastname() {
//    return lastname;
//  }
//
//  public void setLastname(String lastname) {
//    this.lastname = lastname;
//  }
//
//  public char getStatus() {
//    return status;
//  }
//
//  public void setStatus(char status) {
//    this.status = status;
//  }
//
//  public String getEmail() {
//    return email;
//  }
//
//  public void setEmail(String email) {
//    this.email = email;
//  }
//
//  public String getGitUsername() {
//    return gitUsername;
//  }
//
//  public void setGitUsername(String gitUsername) {
//    this.gitUsername = gitUsername;
//  }
//
//  public Set<ContactInformations> getContactInformations() {
//    return contactInformations;
//  }
//
//  public void setContactInformations(Set<ContactInformations> contactInformations) {
//    this.contactInformations = contactInformations;
//  }
//}
