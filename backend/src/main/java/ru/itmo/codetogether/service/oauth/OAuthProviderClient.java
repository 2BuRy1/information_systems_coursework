package ru.itmo.codetogether.service.oauth;

import org.springframework.security.oauth2.client.registration.ClientRegistration;

public interface OAuthProviderClient {

    OAuthUser exchangeCode(ClientRegistration registration, String code, String redirectUri);
}
