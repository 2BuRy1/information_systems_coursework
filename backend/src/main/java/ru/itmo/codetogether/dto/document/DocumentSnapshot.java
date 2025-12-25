package ru.itmo.codetogether.dto.document;

import java.time.Instant;

public record DocumentSnapshot(
    Long id, Long documentId, Integer version, Long userId, Instant createdAt, String content) {}
