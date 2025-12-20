package ru.itmo.codetogether.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.codetogether.dto.UserDto;
import ru.itmo.codetogether.model.UserEntity;
import ru.itmo.codetogether.service.AuthService;

@RestController
@RequestMapping("/users/me")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public UserDto.UserProfile me(@AuthenticationPrincipal UserEntity account) {
        return authService.profile(account);
    }

    @PatchMapping
    public UserDto.UserProfile update(
            @AuthenticationPrincipal UserEntity account, @Valid @RequestBody UserDto.UserUpdateRequest request) {
        return authService.update(account, request);
    }
}
