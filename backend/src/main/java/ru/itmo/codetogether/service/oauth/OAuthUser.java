package ru.itmo.codetogether.service.oauth;

import java.time.Instant;

public record OAuthUser(
        String email, String name, String avatarUrl, String accessToken, String refreshToken, Instant expiresAt) {}

