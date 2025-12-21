package ru.itmo.codetogether.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import ru.itmo.codetogether.dto.auth.AuthTokens;

@Service
public class TokenService {

    private static final Duration ACCESS_TTL = Duration.ofMinutes(30);
    private static final Duration REFRESH_TTL = Duration.ofDays(7);

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, TokenRecord> accessTokens = new ConcurrentHashMap<>();
    private final Map<String, TokenRecord> refreshTokens = new ConcurrentHashMap<>();

    public AuthTokens issueTokens(Long userId) {
        Instant now = Instant.now();
        String access = generateToken();
        String refresh = generateToken();
        accessTokens.put(access, new TokenRecord(userId, now.plus(ACCESS_TTL)));
        refreshTokens.put(refresh, new TokenRecord(userId, now.plus(REFRESH_TTL)));
        return new AuthTokens(access, refresh, ACCESS_TTL.toSeconds());
    }

    public Optional<Long> resolveAccessToken(String token) {
        TokenRecord record = accessTokens.get(token);
        if (record == null) {
            return Optional.empty();
        }
        if (record.expiresAt().isBefore(Instant.now())) {
            accessTokens.remove(token);
            return Optional.empty();
        }
        return Optional.of(record.userId());
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
