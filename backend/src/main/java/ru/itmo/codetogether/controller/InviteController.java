package ru.itmo.codetogether.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.codetogether.dto.SessionDto;
import ru.itmo.codetogether.model.UserEntity;
import ru.itmo.codetogether.service.InviteService;
import ru.itmo.codetogether.service.SessionService;

@RestController
@RequestMapping
public class InviteController {

    private final InviteService inviteService;
    private final SessionService sessionService;

    public InviteController(InviteService inviteService, SessionService sessionService) {
        this.inviteService = inviteService;
        this.sessionService = sessionService;
    }

    @PostMapping("/sessions/{sessionId}/invites")
    public ResponseEntity<SessionDto.InviteLink> create(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable Long sessionId,
            @Valid @RequestBody SessionDto.InviteCreateRequest request) {
        sessionService.ensureOwner(sessionId, user.getId());
        SessionDto.InviteLink link = inviteService.createInvite(sessionId, request.expiresInMinutes());
        return ResponseEntity.status(201).body(link);
    }

    @PostMapping("/invites/{token}/accept")
    public SessionDto.SessionDetails accept(
            @AuthenticationPrincipal UserEntity user, @PathVariable String token) {
        return inviteService.acceptInvite(token, user.getId());
    }

    @GetMapping("/public/sessions/{token}")
    public SessionDto.PublicSessionView publicView(@PathVariable String token) {
        return inviteService.getPublicView(token);
    }
}
