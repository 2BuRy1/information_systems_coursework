package ru.itmo.codetogether.controller;

import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.codetogether.dto.SessionDto;
import ru.itmo.codetogether.model.UserEntity;
import ru.itmo.codetogether.service.SessionService;

@RestController
@RequestMapping("/sessions")
public class SessionsController {

    private final SessionService sessionService;

    public SessionsController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public SessionDto.SessionList list(
            @AuthenticationPrincipal UserEntity user,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) Long cursor) {
        int boundedLimit = Math.max(1, Math.min(100, limit));
        return sessionService.listSessions(user.getId(), Optional.ofNullable(role), boundedLimit, cursor);
    }

    @PostMapping
    public ResponseEntity<SessionDto.SessionDetails> create(
            @AuthenticationPrincipal UserEntity user, @Valid @RequestBody SessionDto.SessionRequest request) {
        return ResponseEntity.status(201).body(sessionService.createSession(user, request));
    }

    @GetMapping("/{sessionId}")
    public SessionDto.SessionDetails get(
            @AuthenticationPrincipal UserEntity user, @PathVariable Long sessionId) {
        return sessionService.getSession(sessionId, user.getId());
    }

    @PatchMapping("/{sessionId}")
    public SessionDto.SessionDetails update(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long sessionId,
            @Valid @RequestBody SessionDto.SessionUpdateRequest request) {
        return sessionService.updateSession(sessionId, user.getId(), request);
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserEntity user, @PathVariable Long sessionId) {
        sessionService.deleteSession(sessionId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
