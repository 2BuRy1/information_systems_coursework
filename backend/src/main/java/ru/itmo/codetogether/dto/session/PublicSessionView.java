package ru.itmo.codetogether.dto.session;

import java.time.Instant;
import ru.itmo.codetogether.dto.document.DocumentState;

public record PublicSessionView(
    Long sessionId,
    String name,
    String language,
    String ownerName,
    Instant expiresAt,
    DocumentState document) {}
