package ru.itmo.codetogether.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.codetogether.dto.auth.AuthResponse;
import ru.itmo.codetogether.dto.auth.OAuthExchangeRequest;
import ru.itmo.codetogether.dto.auth.OAuthUrlResponse;
import ru.itmo.codetogether.exception.CodeTogetherException;
import ru.itmo.codetogether.service.AuthService;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
public class OAuthController {

  private final AuthService authService;

  @GetMapping("/{provider}/url")
  public OAuthUrlResponse url(@PathVariable String provider) {
    return switch (provider.toLowerCase()) {
      case "github" -> authService.githubAuthUrl();
      case "google" -> authService.googleAuthUrl();
      default -> throw new CodeTogetherException(HttpStatus.BAD_REQUEST, "Неизвестный провайдер");
    };
  }

  @PostMapping("/{provider}/exchange")
  public AuthResponse exchange(
      @PathVariable String provider, @Valid @RequestBody OAuthExchangeRequest request) {
    return switch (provider.toLowerCase()) {
      case "github" -> authService.githubExchange(request);
      case "google" -> authService.googleExchange(request);
      default -> throw new CodeTogetherException(HttpStatus.BAD_REQUEST, "Неизвестный провайдер");
    };
  }
}
