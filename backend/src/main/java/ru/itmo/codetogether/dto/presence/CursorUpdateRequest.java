package ru.itmo.codetogether.dto.presence;

import jakarta.validation.constraints.NotNull;

public record CursorUpdateRequest(@NotNull Integer line, @NotNull Integer col, String color) {}
