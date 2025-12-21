package ru.itmo.codetogether.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record OAuthExchangeRequest(
        @NotBlank String code,
        @NotBlank String state,
        @NotBlank String redirectUri) {}
