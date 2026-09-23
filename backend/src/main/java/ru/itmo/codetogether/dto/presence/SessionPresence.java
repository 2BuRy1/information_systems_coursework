package ru.itmo.codetogether.dto.presence;

import java.util.List;

public record SessionPresence(
    Long sessionId, List<CursorState> cursors, List<Highlight> highlights) {}
