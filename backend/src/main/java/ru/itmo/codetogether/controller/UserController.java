package ru.itmo.codetogether.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.codetogether.dto.user.UserProfile;
import ru.itmo.codetogether.dto.user.UserUpdateRequest;
import ru.itmo.codetogether.model.UserEntity;
import ru.itmo.codetogether.service.AuthService;

@RestController
@RequestMapping("/users/me")
@RequiredArgsConstructor
public class UserController {

  private final AuthService authService;

  @GetMapping
  public UserProfile me(@AuthenticationPrincipal UserEntity account) {
    return authService.profile(account);
  }

  @PatchMapping
  public UserProfile update(
      @AuthenticationPrincipal UserEntity account, @Valid @RequestBody UserUpdateRequest request) {
    return authService.update(account, request);
  }
}
