package ru.itmo.codetogether.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.codetogether.dto.session.InviteCreateRequest;
import ru.itmo.codetogether.dto.session.InviteLink;
import ru.itmo.codetogether.dto.session.PublicSessionView;
import ru.itmo.codetogether.dto.session.SessionDetails;
import ru.itmo.codetogether.model.UserEntity;
import ru.itmo.codetogether.service.InviteService;
import ru.itmo.codetogether.service.SessionService;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class InviteController {

  private final InviteService inviteService;
  private final SessionService sessionService;

  @PostMapping("/sessions/{sessionId}/invites")
  public ResponseEntity<InviteLink> create(
      @AuthenticationPrincipal UserEntity user,
      @PathVariable Long sessionId,
      @Valid @RequestBody InviteCreateRequest request) {
    sessionService.ensureOwner(sessionId, user.getId());
    InviteLink link = inviteService.createInvite(sessionId, request.expiresInMinutes());
    return ResponseEntity.status(201).body(link);
  }

  @PostMapping("/invites/{token}/accept")
  public SessionDetails accept(
      @AuthenticationPrincipal UserEntity user, @PathVariable String token) {
    return inviteService.acceptInvite(token, user.getId());
  }

  @GetMapping("/public/sessions/{token}")
  public PublicSessionView publicView(@PathVariable String token) {
    return inviteService.getPublicView(token);
  }
}
