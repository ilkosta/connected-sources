package org.connected_sources.core.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepository {

  private final JdbcTemplate jdbc;

  public UserRepository(JdbcTemplate jdbcTemplate) {
    this.jdbc = jdbcTemplate;
  }

  public Optional<User> findByUserId(String userId) {
      return jdbc.query("select * from app_user where username=?",
              ps -> ps.setString(1,userId),
              rs -> {
                  return rs.next()
                          ? Optional.of(User.fromRecord(rs))
                          : Optional.empty();
              }
      );
  }

}

