package ru.itmo.codetogether.dto.task;

import java.time.Instant;
import java.util.Map;

public record Task(
    Long id,
    Long sessionId,
    String text,
    String status,
    Long userId,
    Map<String, String> metadata,
    Instant createdAt,
    Instant updatedAt) {}
