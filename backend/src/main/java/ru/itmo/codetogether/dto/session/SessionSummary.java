package ru.itmo.codetogether.dto.session;

import java.time.Instant;

public record SessionSummary(
    Long id, String name, String language, Long ownerId, String role, Instant updatedAt) {}
