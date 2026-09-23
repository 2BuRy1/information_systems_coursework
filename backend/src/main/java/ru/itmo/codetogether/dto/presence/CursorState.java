package ru.itmo.codetogether.dto.presence;

import java.time.Instant;

public record CursorState(
    Long sessionId, Long userId, Integer line, Integer col, String color, Instant updatedAt) {}
