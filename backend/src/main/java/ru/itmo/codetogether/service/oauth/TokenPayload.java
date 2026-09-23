package ru.itmo.codetogether.service.oauth;

import java.time.Instant;

record TokenPayload(String accessToken, String refreshToken, Instant expiresAt) {}
