package ru.itmo.codetogether.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class AuthDto {

    private AuthDto() {}

    public static record OAuthUrlResponse(@NotBlank String url, @NotBlank String state) {}

    public static record OAuthExchangeRequest(
            @NotBlank String code,
            @NotBlank String state,
            @NotBlank String redirectUri) {}

    public static record AuthResponse(AuthTokens tokens, UserDto.UserProfile user) {}

    public static record AuthTokens(@NotBlank String accessToken, @NotBlank String refreshToken, @NotNull Long expiresIn) {}
}
