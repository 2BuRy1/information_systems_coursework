package ru.itmo.codetogether.dto.session;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InviteCreateRequest(@NotNull @Min(5) @Max(1440) Integer expiresInMinutes) {}
