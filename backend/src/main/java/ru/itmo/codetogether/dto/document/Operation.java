package ru.itmo.codetogether.dto.document;

import java.time.Instant;

public record Operation(
    Long id,
    Long documentId,
    String operationType,
    Integer nodeCounter,
    Integer nodeSite,
    Long leftNode,
    Long rightNode,
    String value,
    String color,
    Integer version,
    Long userId,
    Instant createdAt) {}
