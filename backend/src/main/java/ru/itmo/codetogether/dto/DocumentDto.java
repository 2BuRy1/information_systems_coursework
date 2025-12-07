package ru.itmo.codetogether.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public final class DocumentDto {

    private DocumentDto() {}

    public static record DocumentState(Long id, Long sessionId, Integer version, String content, Instant updatedAt) {}

    public static record DocumentSnapshot(
            Long id, Long documentId, Integer version, Long userId, Instant createdAt, String content) {}

    public static record SnapshotRequest(@NotNull Integer version, @NotBlank String content) {}

    public static record OperationRequest(@NotNull Integer baseVersion, @NotEmpty List<CrdtDto.OperationInput> operations) {}

    public static record OperationAck(Integer appliedVersion, List<CrdtDto.Operation> operations) {}

    public static record OperationsResponse(Integer fromVersion, Integer toVersion, List<CrdtDto.Operation> operations) {}

    public static record DocumentStats(
            Long sessionId,
            Long documentId,
            Integer activeParticipants,
            Integer operationCount,
            Integer lastSnapshotVersion,
            Integer averageLatencyMs,
            Double typingRate) {}
}
