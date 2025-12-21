package ru.itmo.codetogether.dto.document;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OperationRequest(
    @NotNull Integer baseVersion, @NotEmpty List<OperationInput> operations) {}
