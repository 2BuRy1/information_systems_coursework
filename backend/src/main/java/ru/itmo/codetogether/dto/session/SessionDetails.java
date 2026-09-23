package ru.itmo.codetogether.dto.session;

import java.time.Instant;
import ru.itmo.codetogether.dto.document.DocumentStats;

public record SessionDetails(
    Long id,
    String name,
    String language,
    Long ownerId,
    String role,
    Instant updatedAt,
    String link,
    Instant linkExpiresAt,
    DocumentStats stats) {}
