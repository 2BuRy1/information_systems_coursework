package ru.itmo.codetogether.dto.task;

import java.util.Map;

public record TaskUpdateRequest(String text, String status, Map<String, String> metadata) {}
