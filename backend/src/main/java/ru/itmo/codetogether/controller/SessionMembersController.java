package ru.itmo.codetogether.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.codetogether.dto.session.MemberInviteRequest;
import ru.itmo.codetogether.dto.session.MemberRoleRequest;
import ru.itmo.codetogether.dto.session.SessionMember;
import ru.itmo.codetogether.model.UserEntity;
import ru.itmo.codetogether.service.SessionService;

@RestController
@RequestMapping("/sessions/{sessionId}/members")
@RequiredArgsConstructor
public class SessionMembersController {

  private final SessionService sessionService;

  @GetMapping
  public List<SessionMember> list(@PathVariable Long sessionId) {
    return sessionService.listMembers(sessionId);
  }

  @PostMapping
  public ResponseEntity<SessionMember> add(
      @AuthenticationPrincipal UserEntity user,
      @PathVariable Long sessionId,
      @Valid @RequestBody MemberInviteRequest request) {
    SessionMember member = sessionService.addMember(sessionId, user.getId(), request);
    return ResponseEntity.status(201).body(member);
  }

  @PatchMapping("/{userId}")
  public SessionMember changeRole(
      @AuthenticationPrincipal UserEntity user,
      @PathVariable Long sessionId,
      @PathVariable Long userId,
      @Valid @RequestBody MemberRoleRequest request) {
    return sessionService.updateRole(sessionId, user.getId(), userId, request);
  }

  @DeleteMapping("/{userId}")
  public ResponseEntity<Void> remove(
      @AuthenticationPrincipal UserEntity user,
      @PathVariable Long sessionId,
      @PathVariable Long userId) {
    sessionService.removeMember(sessionId, user.getId(), userId);
    return ResponseEntity.noContent().build();
  }
}
