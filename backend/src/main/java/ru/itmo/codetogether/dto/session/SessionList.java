package ru.itmo.codetogether.dto.session;

import java.util.List;

public record SessionList(List<SessionSummary> items, Long nextCursor) {}
