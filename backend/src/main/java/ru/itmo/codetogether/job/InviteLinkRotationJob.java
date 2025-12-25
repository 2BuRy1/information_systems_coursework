package ru.itmo.codetogether.job;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.itmo.codetogether.model.SessionEntity;
import ru.itmo.codetogether.repository.SessionRepository;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
    prefix = "codetogether.invites",
    name = "auto-rotate-expired",
    havingValue = "true")
public class InviteLinkRotationJob {

  private final SessionRepository sessionRepository;

  @Value("${codetogether.invites.rotate-ttl-minutes:60}")
  private int rotateTtlMinutes;

  @Scheduled(fixedDelayString = "${codetogether.invites.rotate-interval-ms:60000}")
  public void rotateExpiredLinks() {
    Instant now = Instant.now();
    List<SessionEntity> expired = sessionRepository.findByLinkExpiresAtBefore(now);
    if (expired.isEmpty()) {
      return;
    }
    Instant nextExpiresAt = now.plus(Duration.ofMinutes(Math.max(5, rotateTtlMinutes)));
    expired.forEach(
        session -> {
          session.setLink(generateToken());
          session.setLinkExpiresAt(nextExpiresAt);
        });
    sessionRepository.saveAll(expired);
  }

  private String generateToken() {
    return UUID.randomUUID().toString().replace("-", "");
  }
}
