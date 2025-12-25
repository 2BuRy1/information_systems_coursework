package ru.itmo.codetogether.dto.presence;

import jakarta.validation.constraints.NotNull;

public record HighlightRequest(
    @NotNull Integer startLine,
    @NotNull Integer endLine,
    @NotNull Integer startCol,
    @NotNull Integer endCol,
    String color) {}
