package ru.itmo.codetogether.service.oauth;

import java.time.Instant;
import org.springframework.security.oauth2.client.registration.ClientRegistration;

public interface OAuthProviderClient {

    OAuthUser exchangeCode(ClientRegistration registration, String code, String redirectUri);

    record OAuthUser(String email, String name, String avatarUrl, String accessToken, String refreshToken, Instant expiresAt) {}
}
