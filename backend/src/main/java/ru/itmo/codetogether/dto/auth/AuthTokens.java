package ru.itmo.codetogether.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuthTokens(@NotBlank String accessToken, @NotBlank String refreshToken, @NotNull Long expiresIn) {}
