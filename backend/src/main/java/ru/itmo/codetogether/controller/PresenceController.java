package ru.itmo.codetogether.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.codetogether.dto.presence.CursorState;
import ru.itmo.codetogether.dto.presence.CursorUpdateRequest;
import ru.itmo.codetogether.dto.presence.Highlight;
import ru.itmo.codetogether.dto.presence.HighlightRequest;
import ru.itmo.codetogether.dto.presence.SessionPresence;
import ru.itmo.codetogether.model.UserEntity;
import ru.itmo.codetogether.service.PresenceService;
import ru.itmo.codetogether.service.SessionService;

@RestController
@RequestMapping("/sessions/{sessionId}")
@RequiredArgsConstructor
public class PresenceController {

  private final PresenceService presenceService;
  private final SessionService sessionService;

  @GetMapping("/presence")
  public SessionPresence presence(
      @AuthenticationPrincipal UserEntity user, @PathVariable Long sessionId) {
    sessionService.ensureMember(sessionId, user.getId());
    return presenceService.getPresence(sessionId);
  }

  @PutMapping("/presence/cursor")
  public CursorState updateCursor(
      @AuthenticationPrincipal UserEntity user,
      @PathVariable Long sessionId,
      @Valid @RequestBody CursorUpdateRequest request) {
    sessionService.ensureMember(sessionId, user.getId());
    return presenceService.updateCursor(sessionId, user.getId(), request);
  }

  @DeleteMapping("/presence/cursor")
  public ResponseEntity<Void> clearCursor(
      @AuthenticationPrincipal UserEntity user, @PathVariable Long sessionId) {
    sessionService.ensureMember(sessionId, user.getId());
    presenceService.clearCursor(sessionId, user.getId());
    return ResponseEntity.noContent().build();
  }

  @PutMapping("/presence/highlight")
  public Highlight updateHighlight(
      @AuthenticationPrincipal UserEntity user,
      @PathVariable Long sessionId,
      @Valid @RequestBody HighlightRequest request) {
    sessionService.ensureMember(sessionId, user.getId());
    return presenceService.updateHighlight(sessionId, user.getId(), request);
  }

  @DeleteMapping("/presence/highlight")
  public ResponseEntity<Void> clearHighlight(
      @AuthenticationPrincipal UserEntity user, @PathVariable Long sessionId) {
    sessionService.ensureMember(sessionId, user.getId());
    presenceService.clearHighlight(sessionId, user.getId());
    return ResponseEntity.noContent().build();
  }
}
