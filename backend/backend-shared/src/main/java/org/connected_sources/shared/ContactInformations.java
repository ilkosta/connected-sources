package org.connected_sources.shared;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class ContactInformations {

  @Id
  @GeneratedValue
  private Long id;

  @Enumerated(EnumType.STRING)
  private ContactChannel channel;

  private String value;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public ContactChannel getChannel() {
    return channel;
  }

  public void setChannel(ContactChannel channel) {
    this.channel = channel;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ContactInformations that)) return false;
    return Objects.equals(id, that.id) && channel == that.channel && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, channel, value);
  }
}

