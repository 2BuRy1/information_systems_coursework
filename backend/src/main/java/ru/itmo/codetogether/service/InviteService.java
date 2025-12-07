package ru.itmo.codetogether.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.itmo.codetogether.dto.DocumentDto;
import ru.itmo.codetogether.dto.SessionDto;
import ru.itmo.codetogether.exception.CodeTogetherException;
import ru.itmo.codetogether.model.SessionEntity;
import ru.itmo.codetogether.model.UserEntity;

@Service
public class InviteService {

    private final SessionService sessionService;
    private final UserService userService;
    private final DocumentService documentService;

    public InviteService(SessionService sessionService, UserService userService, DocumentService documentService) {
        this.sessionService = sessionService;
        this.userService = userService;
        this.documentService = documentService;
    }

    public SessionDto.InviteLink createInvite(Long sessionId, int expiresInMinutes) {
        SessionEntity session = sessionService.requireSession(sessionId);
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(expiresInMinutes));
        String token = generateToken();
        sessionService.updateInviteLink(session.getId(), token, expiresAt);
        return new SessionDto.InviteLink(sessionId, "/public/sessions/" + token, expiresAt);
    }

    public SessionDto.PublicSessionView getPublicView(String tokenValue) {
        SessionEntity session = findSessionByToken(tokenValue);
        DocumentDto.DocumentState document = documentService.getDocument(session.getId());
        UserEntity owner = userService.findById(session.getOwnerId()).orElse(null);
        return new SessionDto.PublicSessionView(
                session.getId(),
                session.getName(),
                session.getLanguage(),
                owner != null ? owner.getName() : "Unknown",
                session.getLinkExpiresAt(),
                document);
    }

    public SessionDto.SessionDetails acceptInvite(String tokenValue, Long userId) {
        SessionEntity session = findSessionByToken(tokenValue);
        return sessionService.joinByInvite(session.getId(), userId);
    }

    private SessionEntity findSessionByToken(String tokenValue) {
        SessionEntity session = sessionService
                .findByLink(tokenValue)
                .orElseThrow(() -> new CodeTogetherException(HttpStatus.NOT_FOUND, "Ссылка недействительна"));
        if (session.getLinkExpiresAt() == null || session.getLinkExpiresAt().isBefore(Instant.now())) {
            throw new CodeTogetherException(HttpStatus.GONE, "Ссылка протухла");
        }
        return session;
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
