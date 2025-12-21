package ru.itmo.codetogether.dto.document;

public record DocumentStats(
        Long sessionId,
        Long documentId,
        Integer activeParticipants,
        Integer operationCount,
        Integer lastSnapshotVersion,
        Integer averageLatencyMs,
        Double typingRate) {}
