package org.connected_sources.core.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.connected_sources.core.user.model.UserEntity;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // alias, ... for easy refactoring
    default Optional<UserEntity> findByUserId(Long userId) {
        return findById(userId);
    }

    default Optional<UserEntity> findByUserId(String userId) {
        return findByUserId(Long.parseLong(userId));
    }

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);
}
