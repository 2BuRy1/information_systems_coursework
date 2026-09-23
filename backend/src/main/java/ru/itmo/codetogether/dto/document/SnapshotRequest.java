package ru.itmo.codetogether.dto.document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SnapshotRequest(@NotNull Integer version, @NotBlank String content) {}
