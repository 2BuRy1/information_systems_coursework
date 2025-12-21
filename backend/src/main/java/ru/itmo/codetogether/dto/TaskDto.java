package ru.itmo.codetogether.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.Map;

public final class TaskDto {

    private TaskDto() {}

    public static record Task(
            Long id,
            Long sessionId,
            String text,
            String status,
            Long userId,
            Map<String, String> metadata,
            Instant createdAt,
            Instant updatedAt) {}

    public static record TaskRequest(@NotBlank String text, Map<String, String> metadata) {}

    public static record TaskUpdateRequest(String text, String status, Map<String, String> metadata) {}
}
