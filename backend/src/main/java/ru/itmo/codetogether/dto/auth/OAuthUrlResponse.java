package ru.itmo.codetogether.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record OAuthUrlResponse(@NotBlank String url, @NotBlank String state) {}
