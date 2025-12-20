package ru.itmo.codetogether.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.codetogether.model.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
}
