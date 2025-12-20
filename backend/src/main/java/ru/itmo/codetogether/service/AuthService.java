package ru.itmo.codetogether.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.stereotype.Service;
import ru.itmo.codetogether.dto.AuthDto;
import ru.itmo.codetogether.dto.UserDto;
import ru.itmo.codetogether.exception.CodeTogetherException;
import ru.itmo.codetogether.model.OAuthCredentialsEntity;
import ru.itmo.codetogether.model.UserEntity;
import ru.itmo.codetogether.repository.OAuthCredentialsRepository;
import ru.itmo.codetogether.service.oauth.OAuthProviderClient;

@Service
public class AuthService {

    private static final Duration STATE_TTL = Duration.ofMinutes(5);

    private final TokenService tokenService;
    private final UserService userService;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final OAuthProviderClient providerClient;
    private final OAuthCredentialsRepository credentialsRepository;
    private final Map<String, OAuthState> states = new ConcurrentHashMap<>();

    public AuthService(
            TokenService tokenService,
            UserService userService,
            ClientRegistrationRepository clientRegistrationRepository,
            OAuthProviderClient providerClient,
            OAuthCredentialsRepository credentialsRepository) {
        this.tokenService = tokenService;
        this.userService = userService;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.providerClient = providerClient;
        this.credentialsRepository = credentialsRepository;
    }

    public AuthDto.OAuthUrlResponse githubAuthUrl() {
        return buildAuthorizationUrl("github");
    }

    public AuthDto.OAuthUrlResponse googleAuthUrl() {
        return buildAuthorizationUrl("google");
    }

    public AuthDto.AuthResponse githubExchange(AuthDto.OAuthExchangeRequest request) {
        return exchange("github", request);
    }

    public AuthDto.AuthResponse googleExchange(AuthDto.OAuthExchangeRequest request) {
        return exchange("google", request);
    }

    public UserDto.UserProfile profile(UserEntity user) {
        return userService.toProfile(user);
    }

    public UserDto.UserProfile update(UserEntity user, UserDto.UserUpdateRequest request) {
        return userService.updateProfile(user, request);
    }

    private AuthDto.AuthResponse exchange(String provider, AuthDto.OAuthExchangeRequest request) {
        OAuthState state = states.remove(request.state());
        if (state == null || state.expiresAt().isBefore(Instant.now()) || !state.provider().equals(provider)) {
            throw new CodeTogetherException(HttpStatus.BAD_REQUEST, "Некорректный state");
        }
        ClientRegistration registration = getRegistration(provider);
        OAuthProviderClient.OAuthUser oauthUser = providerClient.exchangeCode(registration, request.code(), request.redirectUri());
        UserEntity user = userService.getOrCreateOAuthUser(oauthUser.name(), oauthUser.email().toLowerCase(Locale.ROOT), oauthUser.avatarUrl());
        persistCredentials(user, provider, oauthUser);
        AuthDto.AuthTokens tokens = tokenService.issueTokens(user.getId());
        return new AuthDto.AuthResponse(tokens, userService.toProfile(user));
    }

    private void persistCredentials(UserEntity user, String provider, OAuthProviderClient.OAuthUser oauthUser) {
        OAuthCredentialsEntity credentials = new OAuthCredentialsEntity();
        credentials.setUser(user);
        credentials.setProvider(provider);
        credentials.setAccessToken(oauthUser.accessToken());
        credentials.setRefreshToken(oauthUser.refreshToken());
        credentials.setTokenExpiresAt(oauthUser.expiresAt());
        credentials.setScopes("profile email");
        credentialsRepository.save(credentials);
    }

    private AuthDto.OAuthUrlResponse buildAuthorizationUrl(String provider) {
        ClientRegistration registration = getRegistration(provider);
        String stateToken = UUID.randomUUID().toString();
        states.put(stateToken, new OAuthState(provider, Instant.now().plus(STATE_TTL)));
        String url = registration.getProviderDetails()
                .getAuthorizationUri()
                + "?client_id="
                + registration.getClientId()
                + "&redirect_uri="
                + registration.getRedirectUri()
                + "&response_type=code&scope="
                + String.join(" ", registration.getScopes())
                + "&state="
                + stateToken;
        return new AuthDto.OAuthUrlResponse(url, stateToken);
    }

    private ClientRegistration getRegistration(String provider) {
        ClientRegistration registration = clientRegistrationRepository.findByRegistrationId(provider);
        if (registration == null) {
            throw new CodeTogetherException(HttpStatus.BAD_REQUEST, "Неизвестный провайдер");
        }
        return registration;
    }

    private record OAuthState(String provider, Instant expiresAt) {}
}
