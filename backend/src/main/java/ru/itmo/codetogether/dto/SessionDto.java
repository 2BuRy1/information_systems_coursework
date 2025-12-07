package ru.itmo.codetogether.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public final class SessionDto {

    private SessionDto() {}

    public static record SessionRequest(@NotBlank String name, @NotBlank String language) {}

    public static record SessionUpdateRequest(String name, String language) {}

    public static record SessionSummary(
            Long id, String name, String language, Long ownerId, String role, Instant updatedAt) {}

    public static record SessionDetails(
            Long id,
            String name,
            String language,
            Long ownerId,
            String role,
            Instant updatedAt,
            String link,
            Instant linkExpiresAt,
            DocumentDto.DocumentStats stats) {}

    public static record SessionList(List<SessionSummary> items, Long nextCursor) {}

    public static record SessionMember(
            Long sessionId, Long userId, String role, Instant joinedAt, String name, String avatarUrl) {}

    public static record MemberInviteRequest(String email, Long userId, String role) {}

    public static record MemberRoleRequest(@NotBlank String role) {}

    public static record InviteCreateRequest(@NotNull @Min(5) @Max(1440) Integer expiresInMinutes) {}

    public static record InviteLink(Long sessionId, String link, Instant expiresAt) {}

    public static record PublicSessionView(
            Long sessionId, String name, String language, String ownerName, Instant expiresAt, DocumentDto.DocumentState document) {}
}
