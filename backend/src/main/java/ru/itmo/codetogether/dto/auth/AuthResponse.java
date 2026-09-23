package ru.itmo.codetogether.dto.auth;

import ru.itmo.codetogether.dto.user.UserProfile;

public record AuthResponse(AuthTokens tokens, UserProfile user) {}
