package ru.itmo.codetogether.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public final class PresenceDto {

    private PresenceDto() {}

    public static record CursorUpdateRequest(@NotNull Integer line, @NotNull Integer col, String color) {}

    public static record CursorState(Long sessionId, Long userId, Integer line, Integer col, String color, Instant updatedAt) {}

    public static record HighlightRequest(
            @NotNull Integer startLine,
            @NotNull Integer endLine,
            @NotNull Integer startCol,
            @NotNull Integer endCol,
            String color) {}

    public static record Highlight(
            Long sessionId,
            Long userId,
            Integer startLine,
            Integer endLine,
            Integer startCol,
            Integer endCol,
            String color,
            Instant updatedAt) {}

    public static record SessionPresence(Long sessionId, List<CursorState> cursors, List<Highlight> highlights) {}
}
