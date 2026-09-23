package ru.itmo.codetogether.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.codetogether.dto.user.UserProfile;
import ru.itmo.codetogether.dto.user.UserUpdateRequest;
import ru.itmo.codetogether.exception.CodeTogetherException;
import ru.itmo.codetogether.model.UserEntity;
import ru.itmo.codetogether.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public Optional<UserEntity> findByEmail(String email) {
    if (email == null) {
      return Optional.empty();
    }
    return userRepository.findByEmail(email.toLowerCase());
  }

  public Optional<UserEntity> findById(Long id) {
    return userRepository.findById(id);
  }

  public UserEntity requireUser(Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(
            () -> new CodeTogetherException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
  }

  @Transactional
  public UserEntity getOrCreateOAuthUser(String name, String email, String avatarUrl) {
    return userRepository
        .findByEmail(email.toLowerCase())
        .orElseGet(
            () -> {
              UserEntity entity = new UserEntity();
              entity.setName(resolveName(name, email));
              entity.setEmail(email.toLowerCase());
              entity.setAvatarUrl(avatarUrl);
              entity.setRole("member");
              return userRepository.save(entity);
            });
  }

  @Transactional
  public UserProfile updateProfile(UserEntity user, UserUpdateRequest request) {
    if (request.name() != null && !request.name().isBlank()) {
      user.setName(request.name());
    }
    if (request.avatarUrl() != null && !request.avatarUrl().isBlank()) {
      user.setAvatarUrl(request.avatarUrl());
    }
    userRepository.save(user);
    return toProfile(user);
  }

  public UserProfile toProfile(UserEntity entity) {
    return new UserProfile(
        entity.getId(),
        entity.getName(),
        entity.getEmail(),
        entity.getAvatarUrl(),
        entity.getRole());
  }

  private String resolveName(String providedName, String email) {
    if (providedName != null && !providedName.isBlank()) {
      return providedName.trim();
    }
    int atIndex = email.indexOf('@');
    if (atIndex > 0) {
      return email.substring(0, atIndex);
    }
    return email;
  }
}
