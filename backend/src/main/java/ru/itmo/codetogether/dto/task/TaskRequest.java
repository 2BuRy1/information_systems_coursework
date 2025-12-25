package ru.itmo.codetogether.dto.task;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record TaskRequest(@NotBlank String text, Map<String, String> metadata) {}
