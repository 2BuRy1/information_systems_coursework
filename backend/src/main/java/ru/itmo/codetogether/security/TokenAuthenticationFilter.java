package ru.itmo.codetogether.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.itmo.codetogether.model.UserEntity;
import ru.itmo.codetogether.service.TokenService;
import ru.itmo.codetogether.service.UserService;

@Component
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

  private final TokenService tokenService;
  private final UserService userService;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String path = request.getServletPath();
    if (path != null && (path.startsWith("/ws") || path.startsWith("/public/ws"))) {
      filterChain.doFilter(request, response);
      return;
    }
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7);
      Optional<Long> userId = tokenService.resolveAccessToken(token);
      userId.flatMap(userService::findById).ifPresent(this::authenticate);
    }
    filterChain.doFilter(request, response);
  }

  private void authenticate(UserEntity user) {
    if (SecurityContextHolder.getContext().getAuthentication() == null) {
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }
  }
}
