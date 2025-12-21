package ru.itmo.codetogether.service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import ru.itmo.codetogether.dto.document.DocumentState;
import ru.itmo.codetogether.dto.session.InviteLink;
import ru.itmo.codetogether.dto.session.PublicSessionView;
import ru.itmo.codetogether.dto.session.SessionDetails;
import ru.itmo.codetogether.exception.CodeTogetherException;
import ru.itmo.codetogether.model.SessionEntity;
import ru.itmo.codetogether.model.UserEntity;

@Service
@RequiredArgsConstructor
public class InviteService {

    private final SessionService sessionService;
    private final UserService userService;
    private final DocumentService documentService;

    public InviteLink createInvite(Long sessionId, int expiresInMinutes) {
        SessionEntity session = sessionService.requireSession(sessionId);
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(expiresInMinutes));
        String token = generateToken();
        sessionService.updateInviteLink(session.getId(), token, expiresAt);
        return new InviteLink(sessionId, "/public/sessions/" + token, expiresAt);
    }

    @Transactional(readOnly = true)
    public PublicSessionView getPublicView(String tokenValue) {
        SessionEntity session = findSessionByToken(tokenValue);
        DocumentState document = documentService.getDocument(session.getId());
        UserEntity owner = userService.findById(session.getOwner().getId()).orElse(null);
        return new PublicSessionView(
                session.getId(),
                session.getName(),
                session.getLanguage(),
                owner != null ? owner.getName() : "Unknown",
                session.getLinkExpiresAt(),
                document);
    }

    public SessionDetails acceptInvite(String tokenValue, Long userId) {
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
