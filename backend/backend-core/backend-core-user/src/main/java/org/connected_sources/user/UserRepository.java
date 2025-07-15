package org.connected_sources.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class UserRepository {

  private final JdbcTemplate jdbcTemplate;

  public UserRepository(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public String createProducerAndUser(CreateUserAndProducerCommand command) {
    String producerId = "p__" + UUID.randomUUID().toString().replace("-", "");
    jdbcTemplate.update("INSERT INTO producers (id, name) VALUES (?, ?)",
                        producerId, command.getProducerName());

    String userId = "u__" + UUID.randomUUID().toString().replace("-", "");
    jdbcTemplate.update("INSERT INTO users (id, email, producer_id) VALUES (?, ?, ?)",
                        userId, command.getAdminEmail(), producerId);

    return producerId;
  }

}
