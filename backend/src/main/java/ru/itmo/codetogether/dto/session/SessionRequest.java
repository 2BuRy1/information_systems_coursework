package ru.itmo.codetogether.dto.session;

import jakarta.validation.constraints.NotBlank;

public record SessionRequest(@NotBlank String name, @NotBlank String language) {}
