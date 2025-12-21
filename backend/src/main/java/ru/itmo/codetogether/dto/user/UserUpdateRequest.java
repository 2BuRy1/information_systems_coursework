package ru.itmo.codetogether.dto.user;

import jakarta.validation.constraints.Size;

public record UserUpdateRequest(@Size(min = 1, max = 255) String name, @Size(max = 2048) String avatarUrl) {}
