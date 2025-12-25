package ru.itmo.codetogether.dto.user;

public record UserProfile(Long id, String name, String email, String avatarUrl, String role) {}
