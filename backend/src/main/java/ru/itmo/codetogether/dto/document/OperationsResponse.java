package ru.itmo.codetogether.dto.document;

import java.util.List;

public record OperationsResponse(
    Integer fromVersion, Integer toVersion, List<Operation> operations) {}
