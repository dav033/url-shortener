package io.github.dav033.user_service.repositories;

import io.github.dav033.user_service.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByName(String name);

    Optional<User> findByEmail(String email);

    Boolean existsByName(String name);

    Boolean existsByEmail(String email);
}
