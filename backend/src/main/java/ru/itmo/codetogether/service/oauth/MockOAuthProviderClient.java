package ru.itmo.codetogether.service.oauth;

import java.time.Duration;
import java.time.Instant;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Component;

@Component
@Profile("mock-oauth")
public class MockOAuthProviderClient implements OAuthProviderClient {

  @Override
  public OAuthUser exchangeCode(ClientRegistration registration, String code, String redirectUri) {
    String email = registration.getRegistrationId() + "+" + code + "@codetogether.dev";
    String friendly = code.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    if (friendly.length() > 6) {
      friendly = friendly.substring(0, 6);
    }
    if (friendly.isBlank()) {
      friendly = "GUEST";
    }
    String name = registration.getClientName() + " #" + friendly;
    String avatar = "https://avatars.example.com/" + registration.getRegistrationId() + "/" + code;
    Instant expiresAt = Instant.now().plus(Duration.ofHours(1));
    return new OAuthUser(email, name, avatar, "access-" + code, "refresh-" + code, expiresAt);
  }
}
