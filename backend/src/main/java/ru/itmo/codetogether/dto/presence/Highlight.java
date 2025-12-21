package ru.itmo.codetogether.dto.presence;

import java.time.Instant;

public record Highlight(
        Long sessionId,
        Long userId,
        Integer startLine,
        Integer endLine,
        Integer startCol,
        Integer endCol,
        String color,
        Instant updatedAt) {}
