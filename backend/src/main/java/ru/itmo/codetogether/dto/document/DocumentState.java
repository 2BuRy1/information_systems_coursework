package ru.itmo.codetogether.dto.document;

import java.time.Instant;

public record DocumentState(
    Long id, Long sessionId, Integer version, String content, Instant updatedAt) {}
