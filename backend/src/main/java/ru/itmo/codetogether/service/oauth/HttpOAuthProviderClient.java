package ru.itmo.codetogether.service.oauth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.itmo.codetogether.exception.CodeTogetherException;

@Component
@Profile("!mock-oauth")
public class HttpOAuthProviderClient implements OAuthProviderClient {

    private static final Logger log = LoggerFactory.getLogger(HttpOAuthProviderClient.class);
    private static final String GITHUB = "github";
    private static final String GOOGLE = "google";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public HttpOAuthProviderClient(RestTemplateBuilder builder, ObjectMapper objectMapper) {
        this.restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(10))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public OAuthUser exchangeCode(ClientRegistration registration, String code, String redirectUri) {
        TokenPayload token = fetchToken(registration, code, redirectUri);
        Map<String, Object> userInfo = fetchUserInfo(registration, token.accessToken());

        String email = extractEmail(registration, token.accessToken(), userInfo);
        if (email == null || email.isBlank()) {
            email = fallbackEmail(registration, userInfo);
        }
        String name = extractName(registration, userInfo, email);
        String avatar = extractAvatar(userInfo);
        Instant expiresAt = token.expiresAt() != null ? token.expiresAt() : Instant.now().plus(Duration.ofHours(1));
        return new OAuthUser(email, name, avatar, token.accessToken(), token.refreshToken(), expiresAt);
    }

    private TokenPayload fetchToken(ClientRegistration registration, String code, String redirectUri) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", registration.getClientId());
        body.add("client_secret", registration.getClientSecret());
        body.add("code", code);
        body.add("redirect_uri", redirectUri);
        if (GOOGLE.equals(registration.getRegistrationId())) {
            body.add("grant_type", "authorization_code");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    registration.getProviderDetails().getTokenUri(), request, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new CodeTogetherException(org.springframework.http.HttpStatus.BAD_GATEWAY, "Не удалось получить токен OAuth");
            }
            Map<String, Object> payload = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            String accessToken = asString(payload.get("access_token"));
            if (accessToken == null) {
                throw new CodeTogetherException(org.springframework.http.HttpStatus.BAD_GATEWAY, "Ответ OAuth без access_token");
            }
            String refreshToken = asString(payload.get("refresh_token"));
            Long expiresIn = asLong(payload.get("expires_in"));
            Instant expiresAt = expiresIn != null ? Instant.now().plusSeconds(expiresIn) : null;
            return new TokenPayload(accessToken, refreshToken, expiresAt);
        } catch (IOException | RestClientException exception) {
            log.warn("OAuth token exchange failed", exception);
            throw new CodeTogetherException(org.springframework.http.HttpStatus.BAD_GATEWAY, "Не удалось выполнить обмен токена");
        }
    }

    private Map<String, Object> fetchUserInfo(ClientRegistration registration, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    registration.getProviderDetails().getUserInfoEndpoint().getUri(), HttpMethod.GET, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new CodeTogetherException(org.springframework.http.HttpStatus.BAD_GATEWAY, "Не удалось получить профиль пользователя");
            }
            return objectMapper.readValue(response.getBody(), new TypeReference<>() {});
        } catch (IOException | RestClientException exception) {
            log.warn("OAuth user info request failed", exception);
            throw new CodeTogetherException(org.springframework.http.HttpStatus.BAD_GATEWAY, "Не удалось загрузить профиль OAuth");
        }
    }

    private String extractEmail(ClientRegistration registration, String accessToken, Map<String, Object> userInfo) {
        String email = asString(userInfo.get("email"));
        if (email != null && !email.isBlank()) {
            return email;
        }
        if (GITHUB.equals(registration.getRegistrationId())) {
            return fetchGithubEmail(accessToken);
        }
        return email;
    }

    private String fetchGithubEmail(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Void> request = new HttpEntity<>(headers);
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.github.com/user/emails", HttpMethod.GET, request, String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                return null;
            }
            List<Map<String, Object>> emails = objectMapper.readValue(response.getBody(), new TypeReference<>() {});
            return emails.stream()
                    .filter(entry -> Boolean.TRUE.equals(entry.get("primary")))
                    .map(entry -> asString(entry.get("email")))
                    .filter(v -> v != null && !v.isBlank())
                    .findFirst()
                    .orElseGet(() -> emails.stream()
                            .map(entry -> asString(entry.get("email")))
                            .filter(v -> v != null && !v.isBlank())
                            .findFirst()
                            .orElse(null));
        } catch (IOException | RestClientException exception) {
            log.debug("GitHub email lookup failed", exception);
            return null;
        }
    }

    private String fallbackEmail(ClientRegistration registration, Map<String, Object> userInfo) {
        String login = asString(userInfo.get("login"));
        if (login == null || login.isBlank()) {
            login = Optional.ofNullable(asString(userInfo.get("id")))
                    .orElse(UUID.randomUUID().toString());
        }
        if (GITHUB.equals(registration.getRegistrationId())) {
            return login + "@users.noreply.github.com";
        }
        return login + "@" + registration.getRegistrationId() + ".oauth";
    }

    private String extractName(ClientRegistration registration, Map<String, Object> userInfo, String email) {
        String name = asString(userInfo.get("name"));
        if (name != null && !name.isBlank()) {
            return name;
        }
        String fallback = asString(userInfo.get("login"));
        if (fallback == null || fallback.isBlank()) {
            fallback = email != null ? email.substring(0, email.indexOf('@')) : registration.getClientName();
        }
        return fallback;
    }

    private String extractAvatar(Map<String, Object> userInfo) {
        return Optional.ofNullable(asString(userInfo.get("avatar_url")))
                .or(() -> Optional.ofNullable(asString(userInfo.get("picture"))))
                .orElse(null);
    }

    private String asString(Object value) {
        if (value instanceof String str) {
            return str;
        }
        return value != null ? String.valueOf(value) : null;
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String str) {
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

}
