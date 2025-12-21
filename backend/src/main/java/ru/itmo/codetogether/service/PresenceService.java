package ru.itmo.codetogether.service;

import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import ru.itmo.codetogether.dto.presence.CursorState;
import ru.itmo.codetogether.dto.presence.CursorUpdateRequest;
import ru.itmo.codetogether.dto.presence.Highlight;
import ru.itmo.codetogether.dto.presence.HighlightRequest;
import ru.itmo.codetogether.dto.presence.SessionPresence;
import ru.itmo.codetogether.model.CursorStateEntity;
import ru.itmo.codetogether.model.CursorStateId;
import ru.itmo.codetogether.model.HighlightEntity;
import ru.itmo.codetogether.model.HighlightId;
import ru.itmo.codetogether.model.SessionEntity;
import ru.itmo.codetogether.model.UserEntity;
import ru.itmo.codetogether.repository.CursorStateRepository;
import ru.itmo.codetogether.repository.HighlightRepository;
import ru.itmo.codetogether.repository.SessionRepository;
import ru.itmo.codetogether.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final CursorStateRepository cursorRepository;
    private final HighlightRepository highlightRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public SessionPresence getPresence(Long sessionId) {
        List<CursorState> cursors = cursorRepository.findBySession_Id(sessionId).stream()
                .map(entity -> new CursorState(
                        entity.getSession().getId(),
                        entity.getUser().getId(),
                        entity.getLine(),
                        entity.getCol(),
                        entity.getColor(),
                        entity.getUpdatedAt()))
                .toList();
        List<Highlight> highlights = highlightRepository.findBySession_Id(sessionId).stream()
                .map(entity -> new Highlight(
                        entity.getSession().getId(),
                        entity.getUser().getId(),
                        entity.getStartLine(),
                        entity.getEndLine(),
                        entity.getStartCol(),
                        entity.getEndCol(),
                        entity.getColor(),
                        entity.getUpdatedAt()))
                .toList();
        return new SessionPresence(sessionId, cursors, highlights);
    }

    @Transactional
    public CursorState updateCursor(Long sessionId, Long userId, CursorUpdateRequest request) {
        CursorStateId id = new CursorStateId(sessionId, userId);
        CursorStateEntity entity = cursorRepository
                .findById(id)
                .orElseGet(() -> buildCursorEntity(sessionId, userId));
        entity.setLine(request.line());
        entity.setCol(request.col());
        entity.setColor(request.color() != null ? request.color() : defaultColor(userId));
        entity.touch();
        CursorStateEntity saved = cursorRepository.save(entity);
        return new CursorState(
                saved.getSession().getId(), saved.getUser().getId(), saved.getLine(), saved.getCol(), saved.getColor(), saved.getUpdatedAt());
    }

    @Transactional
    public void clearCursor(Long sessionId, Long userId) {
        cursorRepository.deleteById(new CursorStateId(sessionId, userId));
    }

    @Transactional
    public Highlight updateHighlight(Long sessionId, Long userId, HighlightRequest request) {
        HighlightId id = new HighlightId(sessionId, userId);
        HighlightEntity entity = highlightRepository
                .findById(id)
                .orElseGet(() -> buildHighlightEntity(sessionId, userId));
        entity.setStartLine(request.startLine());
        entity.setEndLine(request.endLine());
        entity.setStartCol(request.startCol());
        entity.setEndCol(request.endCol());
        entity.setColor(request.color() != null ? request.color() : defaultColor(userId));
        entity.touch();
        HighlightEntity saved = highlightRepository.save(entity);
        return new Highlight(
                saved.getSession().getId(),
                saved.getUser().getId(),
                saved.getStartLine(),
                saved.getEndLine(),
                saved.getStartCol(),
                saved.getEndCol(),
                saved.getColor(),
                saved.getUpdatedAt());
    }

    @Transactional
    public void clearHighlight(Long sessionId, Long userId) {
        highlightRepository.deleteById(new HighlightId(sessionId, userId));
    }

    private CursorStateEntity buildCursorEntity(Long sessionId, Long userId) {
        CursorStateEntity entity = new CursorStateEntity();
        entity.setId(new CursorStateId(sessionId, userId));
        SessionEntity session = sessionRepository.getReferenceById(sessionId);
        UserEntity user = userRepository.getReferenceById(userId);
        entity.setSession(session);
        entity.setUser(user);
        entity.setLine(0);
        entity.setCol(0);
        entity.setColor(defaultColor(userId));
        return entity;
    }

    private HighlightEntity buildHighlightEntity(Long sessionId, Long userId) {
        HighlightEntity entity = new HighlightEntity();
        entity.setId(new HighlightId(sessionId, userId));
        entity.setSession(sessionRepository.getReferenceById(sessionId));
        entity.setUser(userRepository.getReferenceById(userId));
        entity.setStartLine(0);
        entity.setEndLine(0);
        entity.setStartCol(0);
        entity.setEndCol(0);
        entity.setColor(defaultColor(userId));
        return entity;
    }

    private String defaultColor(Long userId) {
        int hash = Math.abs(userId.hashCode());
        return String.format("#%06x", hash & 0xFFFFFF);
    }
}
