package ru.itmo.codetogether.service.oauth;

import java.time.Duration;
import java.time.Instant;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.stereotype.Component;

@Component
public class MockOAuthProviderClient implements OAuthProviderClient {

    @Override
    public OAuthUser exchangeCode(ClientRegistration registration, String code, String redirectUri) {
        String email = registration.getRegistrationId() + "+" + code + "@codetogether.dev";
        String name = registration.getClientName() + " User";
        String avatar = "https://avatars.example.com/" + registration.getRegistrationId() + "/" + code;
        Instant expiresAt = Instant.now().plus(Duration.ofHours(1));
        return new OAuthUser(email, name, avatar, "access-" + code, "refresh-" + code, expiresAt);
    }
}
