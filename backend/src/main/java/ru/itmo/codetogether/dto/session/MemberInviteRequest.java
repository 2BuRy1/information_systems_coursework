package ru.itmo.codetogether.dto.session;

public record MemberInviteRequest(String email, Long userId, String role) {}
