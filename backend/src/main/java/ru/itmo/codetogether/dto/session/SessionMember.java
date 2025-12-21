package ru.itmo.codetogether.dto.session;

import java.time.Instant;

public record SessionMember(Long sessionId, Long userId, String role, Instant joinedAt, String name, String avatarUrl) {}
