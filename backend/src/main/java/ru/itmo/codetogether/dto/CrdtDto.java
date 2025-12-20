package ru.itmo.codetogether.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public final class CrdtDto {

    private CrdtDto() {}

    public static record OperationInput(
            @NotBlank String operationType,
            @NotNull Integer nodeCounter,
            @NotNull Integer nodeSite,
            Long leftNode,
            Long rightNode,
            @NotNull String value,
            String color) {}

    public static record Operation(
            Long id,
            Long documentId,
            String operationType,
            Integer nodeCounter,
            Integer nodeSite,
            Long leftNode,
            Long rightNode,
            String value,
            String color,
            Integer version,
            Long userId,
            Instant createdAt) {}
}
