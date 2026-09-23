package ru.itmo.codetogether.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.codetogether.model.SessionEntity;

public interface SessionRepository extends JpaRepository<SessionEntity, Long> {
  Optional<SessionEntity> findByLink(String link);

  List<SessionEntity> findByLinkExpiresAtBefore(Instant instant);
}
