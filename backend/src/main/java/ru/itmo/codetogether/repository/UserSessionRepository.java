package ru.itmo.codetogether.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.codetogether.model.UserSessionEntity;
import ru.itmo.codetogether.model.UserSessionId;

public interface UserSessionRepository extends JpaRepository<UserSessionEntity, UserSessionId> {
  List<UserSessionEntity> findByUser_Id(Long userId);

  List<UserSessionEntity> findBySession_Id(Long sessionId);

  Optional<UserSessionEntity> findBySession_IdAndUser_Id(Long sessionId, Long userId);
}
