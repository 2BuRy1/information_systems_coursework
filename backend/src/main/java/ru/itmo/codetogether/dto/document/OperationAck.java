package ru.itmo.codetogether.dto.document;

import java.util.List;

public record OperationAck(Integer appliedVersion, List<Operation> operations) {}
