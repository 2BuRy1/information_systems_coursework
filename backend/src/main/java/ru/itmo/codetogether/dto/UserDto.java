package ru.itmo.codetogether.dto;

import jakarta.validation.constraints.Size;

public final class UserDto {

    private UserDto() {}

    public static record UserProfile(Long id, String name, String email, String avatarUrl, String role) {}

    public static record UserUpdateRequest(@Size(min = 1, max = 255) String name, @Size(max = 2048) String avatarUrl) {}
}
