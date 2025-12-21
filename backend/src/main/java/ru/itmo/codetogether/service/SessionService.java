package ru.itmo.codetogether.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.codetogether.dto.document.DocumentStats;
import ru.itmo.codetogether.dto.session.MemberInviteRequest;
import ru.itmo.codetogether.dto.session.MemberRoleRequest;
import ru.itmo.codetogether.dto.session.SessionDetails;
import ru.itmo.codetogether.dto.session.SessionList;
import ru.itmo.codetogether.dto.session.SessionMember;
import ru.itmo.codetogether.dto.session.SessionRequest;
import ru.itmo.codetogether.dto.session.SessionSummary;
import ru.itmo.codetogether.dto.session.SessionUpdateRequest;
import ru.itmo.codetogether.exception.CodeTogetherException;
import ru.itmo.codetogether.model.DocumentEntity;
import ru.itmo.codetogether.model.SessionEntity;
import ru.itmo.codetogether.model.SessionRole;
import ru.itmo.codetogether.model.UserEntity;
import ru.itmo.codetogether.model.UserSessionEntity;
import ru.itmo.codetogether.model.UserSessionId;
import ru.itmo.codetogether.repository.DocumentRepository;
import ru.itmo.codetogether.repository.SessionRepository;
import ru.itmo.codetogether.repository.UserRepository;
import ru.itmo.codetogether.repository.UserSessionRepository;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserSessionRepository userSessionRepository;
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final DocumentService documentService;

    @Transactional
    public SessionDetails createSession(UserEntity owner, SessionRequest request) {
        SessionEntity session = new SessionEntity();
        session.setName(request.name());
        session.setLanguage(request.language());
        session.setOwner(owner);
        session.setLink(generateToken());
        session.setLinkExpiresAt(Instant.now().plus(Duration.ofHours(1)));
        SessionEntity savedSession = sessionRepository.save(session);

        DocumentEntity document = new DocumentEntity();
        document.setSession(savedSession);
        DocumentEntity savedDocument = documentRepository.save(document);
        savedSession.setDocument(savedDocument);
        documentService.initializeSequence(savedDocument);
        initializeForDetails(savedSession);

        UserSessionEntity membership = new UserSessionEntity();
        membership.setId(new UserSessionId(savedSession.getId(), owner.getId()));
        membership.setSession(savedSession);
        membership.setUser(owner);
        membership.setRole(SessionRole.OWNER.getValue());
        userSessionRepository.save(membership);

        return toDetails(savedSession, owner.getId());
    }

    @Transactional(readOnly = true)
    public SessionList listSessions(Long userId, Optional<String> roleFilter, int limit, Long cursor) {
        List<UserSessionEntity> memberships = userSessionRepository.findByUser_Id(userId);
        var comparator = Comparator.comparing((UserSessionEntity entity) -> entity.getSession().getUpdatedAt()).reversed();
        List<SessionSummary> summaries = memberships.stream()
                .filter(membership -> roleFilter.map(role -> membership.getRole().equalsIgnoreCase(role)).orElse(true))
                .sorted(comparator)
                .map(membership -> toSummary(membership.getSession(), SessionRole.fromString(membership.getRole())))
                .toList();
        int startIndex = 0;
        if (cursor != null) {
            for (int i = 0; i < summaries.size(); i++) {
                if (summaries.get(i).id().equals(cursor)) {
                    startIndex = i + 1;
                    break;
                }
            }
        }
        int endIndex = Math.min(startIndex + limit, summaries.size());
        List<SessionSummary> page = summaries.subList(startIndex, endIndex);
        Long nextCursor = endIndex < summaries.size() ? page.get(page.size() - 1).id() : null;
        return new SessionList(List.copyOf(page), nextCursor);
    }

    @Transactional(readOnly = true)
    public SessionDetails getSession(Long sessionId, Long userId) {
        SessionEntity session = requireSession(sessionId);
        ensureMember(sessionId, userId);
        initializeForDetails(session);
        return toDetails(session, userId);
    }

    @Transactional
    public SessionDetails updateSession(Long sessionId, Long userId, SessionUpdateRequest request) {
        SessionEntity session = requireSession(sessionId);
        ensureOwner(sessionId, userId);
        if (request.name() != null && !request.name().isBlank()) {
            session.setName(request.name());
        }
        if (request.language() != null && !request.language().isBlank()) {
            session.setLanguage(request.language());
        }
        session.touch();
        sessionRepository.save(session);
        return toDetails(session, userId);
    }

    @Transactional
    public void deleteSession(Long sessionId, Long userId) {
        ensureOwner(sessionId, userId);
        sessionRepository.deleteById(sessionId);
    }

    @Transactional(readOnly = true)
    public List<SessionMember> listMembers(Long sessionId) {
        requireSession(sessionId);
        return userSessionRepository.findBySession_Id(sessionId).stream()
                .map(membership -> {
                    UserEntity user = membership.getUser();
                    user.getId(); // initialize proxy
                    return new SessionMember(
                            membership.getSession().getId(),
                            user.getId(),
                            membership.getRole(),
                            Instant.now(),
                            user.getName(),
                            user.getAvatarUrl());
                })
                .toList();
    }

    @Transactional
    public SessionMember addMember(Long sessionId, Long actorId, MemberInviteRequest request) {
        ensureOwner(sessionId, actorId);
        SessionEntity session = requireSession(sessionId);
        Long userId = resolveTargetUser(request);
        UserEntity user = userRepository
                .findById(userId)
                .orElseThrow(() -> new CodeTogetherException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        UserSessionEntity membership = userSessionRepository
                .findBySession_IdAndUser_Id(sessionId, userId)
                .orElseGet(() -> {
                    UserSessionEntity entity = new UserSessionEntity();
                    entity.setId(new UserSessionId(sessionId, userId));
                    entity.setSession(session);
                    entity.setUser(user);
                    entity.setRole(SessionRole.EDITOR.getValue());
                    return entity;
                });
        if (request.role() != null) {
            membership.setRole(SessionRole.fromString(request.role()).getValue());
        }
        userSessionRepository.save(membership);
        user.getId();
        return new SessionMember(sessionId, userId, membership.getRole(), Instant.now(), user.getName(), user.getAvatarUrl());
    }

    @Transactional
    public SessionMember updateRole(Long sessionId, Long actorId, Long targetUserId, MemberRoleRequest request) {
        ensureOwner(sessionId, actorId);
        UserSessionEntity membership = userSessionRepository
                .findBySession_IdAndUser_Id(sessionId, targetUserId)
                .orElseThrow(() -> new CodeTogetherException(HttpStatus.NOT_FOUND, "Участник не найден"));
        if (SessionRole.fromString(membership.getRole()) == SessionRole.OWNER) {
            throw new CodeTogetherException(HttpStatus.BAD_REQUEST, "Нельзя изменить владельца");
        }
        membership.setRole(SessionRole.fromString(request.role()).getValue());
        userSessionRepository.save(membership);
        UserEntity user = membership.getUser();
        user.getId();
        return new SessionMember(sessionId, user.getId(), membership.getRole(), Instant.now(), user.getName(), user.getAvatarUrl());
    }

    @Transactional
    public void removeMember(Long sessionId, Long actorId, Long targetUserId) {
        ensureOwner(sessionId, actorId);
        if (targetUserId.equals(requireSession(sessionId).getOwner().getId())) {
            throw new CodeTogetherException(HttpStatus.BAD_REQUEST, "Нельзя удалить владельца");
        }
        userSessionRepository
                .findBySession_IdAndUser_Id(sessionId, targetUserId)
                .ifPresent(userSessionRepository::delete);
    }

    public void ensureMember(Long sessionId, Long userId) {
        userSessionRepository
                .findBySession_IdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new CodeTogetherException(HttpStatus.FORBIDDEN, "Нет доступа к доске"));
    }

    public void ensureEditor(Long sessionId, Long userId) {
        UserSessionEntity membership = userSessionRepository
                .findBySession_IdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new CodeTogetherException(HttpStatus.FORBIDDEN, "Нет доступа"));
        if (SessionRole.fromString(membership.getRole()) == SessionRole.VIEWER) {
            throw new CodeTogetherException(HttpStatus.FORBIDDEN, "Требуются права редактирования");
        }
    }

    public void ensureOwner(Long sessionId, Long userId) {
        UserSessionEntity membership = userSessionRepository
                .findBySession_IdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new CodeTogetherException(HttpStatus.FORBIDDEN, "Нет доступа"));
        if (SessionRole.fromString(membership.getRole()) != SessionRole.OWNER) {
            throw new CodeTogetherException(HttpStatus.FORBIDDEN, "Доступ только для владельца");
        }
    }

    @Transactional
    public SessionDetails joinByInvite(Long sessionId, Long userId) {
        SessionEntity session = requireSession(sessionId);
        userSessionRepository
                .findBySession_IdAndUser_Id(sessionId, userId)
                .orElseGet(() -> {
                    UserEntity user = userRepository
                            .findById(userId)
                            .orElseThrow(() -> new CodeTogetherException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
                    UserSessionEntity membership = new UserSessionEntity();
                    membership.setId(new UserSessionId(sessionId, userId));
                    membership.setSession(session);
                    membership.setUser(user);
                    membership.setRole(SessionRole.VIEWER.getValue());
                    return userSessionRepository.save(membership);
                });
        return toDetails(session, userId);
    }

    @Transactional(readOnly = true)
    public int memberCount(Long sessionId) {
        return userSessionRepository.findBySession_Id(sessionId).size();
    }

    @Transactional
    public void updateInviteLink(Long sessionId, String token, Instant expiresAt) {
        SessionEntity session = requireSession(sessionId);
        session.setLink(token);
        session.setLinkExpiresAt(expiresAt);
        sessionRepository.save(session);
    }

    public Optional<SessionEntity> findByLink(String token) {
        return sessionRepository.findByLink(token);
    }

    public SessionEntity requireSession(Long sessionId) {
        return sessionRepository
                .findById(sessionId)
                .orElseThrow(() -> new CodeTogetherException(HttpStatus.NOT_FOUND, "Доска не найдена"));
    }

    private SessionDetails toDetails(SessionEntity session, Long userId) {
        initializeForDetails(session);
        SessionRole role = userSessionRepository
                .findBySession_IdAndUser_Id(session.getId(), userId)
                .map(entity -> SessionRole.fromString(entity.getRole()))
                .orElse(SessionRole.VIEWER);
        DocumentStats stats = documentService.documentStats(session.getId(), memberCount(session.getId()));
        return new SessionDetails(
                session.getId(),
                session.getName(),
                session.getLanguage(),
                session.getOwner().getId(),
                role.getValue(),
                session.getUpdatedAt(),
                session.getLink(),
                session.getLinkExpiresAt(),
                stats);
    }

    private SessionSummary toSummary(SessionEntity session, SessionRole role) {
        initializeForDetails(session);
        return new SessionSummary(
                session.getId(), session.getName(), session.getLanguage(), session.getOwner().getId(), role.getValue(), session.getUpdatedAt());
    }

    private void initializeForDetails(SessionEntity session) {
        if (session == null) {
            return;
        }
        UserEntity owner = session.getOwner();
        if (owner != null) {
            owner.getId();
            owner.getName();
        }
        DocumentEntity document = session.getDocument();
        if (document != null) {
            document.getId();
            document.getVersion();
        }
    }

    private Long resolveTargetUser(MemberInviteRequest request) {
        if (request.userId() != null) {
            return request.userId();
        }
        if (request.email() != null) {
            return userRepository
                    .findByEmail(request.email().toLowerCase())
                    .map(UserEntity::getId)
                    .orElseThrow(() -> new CodeTogetherException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        }
        throw new CodeTogetherException(HttpStatus.BAD_REQUEST, "Нужно указать email или userId");
    }

    private String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
