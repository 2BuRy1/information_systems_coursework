package ru.itmo.codetogether.dto.session;

import java.time.Instant;

public record InviteLink(Long sessionId, String link, Instant expiresAt) {}
