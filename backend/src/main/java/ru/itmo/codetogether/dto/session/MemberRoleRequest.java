package ru.itmo.codetogether.dto.session;

import jakarta.validation.constraints.NotBlank;

public record MemberRoleRequest(@NotBlank String role) {}
