package ru.itmo.codetogether.dto.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OperationInput(
    @NotBlank String operationType,
    @NotNull Integer nodeCounter,
    @NotNull Integer nodeSite,
    Long leftNode,
    Long rightNode,
    @NotNull String value,
    String color) {}
