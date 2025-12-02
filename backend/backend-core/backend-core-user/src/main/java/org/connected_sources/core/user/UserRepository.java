package org.connected_sources.core.user;

import org.connected_sources.core.user.model.User;
import org.connected_sources.core.user.model.UserRowMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

  private final JdbcTemplate jdbc;

  public UserRepository(JdbcTemplate jdbcTemplate) {
    this.jdbc = jdbcTemplate;
  }

  public Optional<User> findByUserId(Long userId) {
//      try {
//          User user = jdbc.queryForObject(
//                  "select * from app_user where user_id = ?",
//                  new UserRowMapper(),
//                  userId
//          );
//          return Optional.ofNullable(user);
//      } catch (EmptyResultDataAccessException e) {
//          return Optional.empty();
//      }

      List<User> results = jdbc.query(
              "select * from app_user where user_id = ?",
              new UserRowMapper(),
              userId
      );

      return results.isEmpty()
              ? Optional.empty()
              : Optional.of(results.getFirst());
  }

  public Optional<User> findByUserId(String userId) {
      return findByUserId(Long.parseLong(userId));
    }

}

