package com.coopshield.soc.identity.application;

import com.coopshield.soc.audit.application.AuditPort;
import com.coopshield.soc.audit.domain.AuditEvent;
import com.coopshield.soc.audit.domain.AuditEventType;
import com.coopshield.soc.identity.domain.AccountLockedException;
import com.coopshield.soc.identity.domain.AccountLockoutPolicy;
import com.coopshield.soc.identity.domain.InvalidCredentialsException;
import com.coopshield.soc.identity.domain.InvalidRefreshTokenException;
import com.coopshield.soc.identity.domain.RefreshToken;
import com.coopshield.soc.identity.domain.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de uso de autenticacao: login, renovacao (refresh) e encerramento
 * de sessao (logout). Orquestra o dominio ({@link User},
 * {@link AccountLockoutPolicy}, {@link RefreshToken}) e as portas de saida
 * (repositorios, emissor de access token, auditoria), sem conhecer
 * detalhes de JWT, MongoDB ou HTTP.
 */
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessTokenIssuer accessTokenIssuer;
    private final PasswordEncoder passwordEncoder;
    private final AccountLockoutPolicy lockoutPolicy;
    private final AuditPort auditPort;
    private final Duration refreshTokenTtl;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthenticationService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            AccessTokenIssuer accessTokenIssuer,
            PasswordEncoder passwordEncoder,
            AccountLockoutPolicy lockoutPolicy,
            AuditPort auditPort,
            Duration refreshTokenTtl
    ) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.refreshTokenRepository = Objects.requireNonNull(refreshTokenRepository);
        this.accessTokenIssuer = Objects.requireNonNull(accessTokenIssuer);
        this.passwordEncoder = Objects.requireNonNull(passwordEncoder);
        this.lockoutPolicy = Objects.requireNonNull(lockoutPolicy);
        this.auditPort = Objects.requireNonNull(auditPort);
        this.refreshTokenTtl = Objects.requireNonNull(refreshTokenTtl);
    }

    public AuthenticationResult login(String username, String rawPassword) {
        Instant now = Instant.now();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null || !user.enabled()) {
            auditPort.record(AuditEvent.of(AuditEventType.AUTHENTICATION_FAILURE, username, Map.of("reason", "unknown_or_disabled_user")));
            throw new InvalidCredentialsException();
        }

        if (user.isLockedAt(now)) {
            auditPort.record(AuditEvent.of(AuditEventType.ACCOUNT_LOCKED, username, Map.of()));
            throw new AccountLockedException();
        }

        if (!passwordEncoder.matches(rawPassword, user.passwordHash())) {
            lockoutPolicy.registerFailedAttempt(user, now);
            userRepository.save(user);
            auditPort.record(AuditEvent.of(
                    AuditEventType.AUTHENTICATION_FAILURE,
                    username,
                    Map.of("failedAttempts", String.valueOf(user.failedLoginAttempts()))));
            throw new InvalidCredentialsException();
        }

        lockoutPolicy.registerSuccessfulAttempt(user);
        userRepository.save(user);
        auditPort.record(AuditEvent.of(AuditEventType.AUTHENTICATION_SUCCESS, username, Map.of()));

        return issueTokensFor(user, now);
    }

    public AuthenticationResult refresh(String rawRefreshToken) {
        Instant now = Instant.now();
        ParsedRefreshToken parsed = parse(rawRefreshToken);

        RefreshToken stored = refreshTokenRepository.findById(parsed.tokenId()).orElse(null);
        if (stored == null) {
            auditPort.record(AuditEvent.of(AuditEventType.TOKEN_REFRESH_DENIED, "unknown", Map.of()));
            throw new InvalidRefreshTokenException();
        }

        String candidateHash = RefreshTokenSecretHasher.hash(parsed.secret());
        if (!stored.isValidAt(now, candidateHash)) {
            auditPort.record(AuditEvent.of(AuditEventType.TOKEN_REFRESH_DENIED, stored.userId().toString(), Map.of()));
            throw new InvalidRefreshTokenException();
        }

        User user = userRepository.findById(stored.userId()).orElse(null);
        if (user == null || !user.enabled()) {
            auditPort.record(AuditEvent.of(AuditEventType.TOKEN_REFRESH_DENIED, stored.userId().toString(), Map.of()));
            throw new InvalidRefreshTokenException();
        }

        // Rotacao: o refresh token usado e sempre revogado, mesmo em caso de sucesso.
        stored.revoke();
        refreshTokenRepository.save(stored);

        auditPort.record(AuditEvent.of(AuditEventType.TOKEN_REFRESHED, user.username(), Map.of()));

        return issueTokensFor(user, now);
    }

    public void logout(String rawRefreshToken) {
        ParsedRefreshToken parsed;
        try {
            parsed = parse(rawRefreshToken);
        } catch (InvalidRefreshTokenException e) {
            return;
        }

        refreshTokenRepository.findById(parsed.tokenId()).ifPresent(token -> {
            token.revoke();
            refreshTokenRepository.save(token);
            auditPort.record(AuditEvent.of(AuditEventType.LOGOUT, token.userId().toString(), Map.of()));
        });
    }

    private AuthenticationResult issueTokensFor(User user, Instant now) {
        AccessToken accessToken = accessTokenIssuer.issue(user.userId(), user.username(), user.role());

        UUID tokenId = UUID.randomUUID();
        byte[] secretBytes = new byte[32];
        secureRandom.nextBytes(secretBytes);
        String secret = Base64.getUrlEncoder().withoutPadding().encodeToString(secretBytes);
        Instant refreshExpiresAt = now.plus(refreshTokenTtl);

        RefreshToken refreshToken = new RefreshToken(tokenId, user.userId(), RefreshTokenSecretHasher.hash(secret), refreshExpiresAt);
        refreshTokenRepository.save(refreshToken);

        String rawRefreshToken = tokenId + "." + secret;
        return new AuthenticationResult(accessToken.value(), accessToken.expiresAt(), rawRefreshToken, refreshExpiresAt);
    }

    private ParsedRefreshToken parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidRefreshTokenException();
        }
        int dot = raw.indexOf('.');
        if (dot <= 0 || dot == raw.length() - 1) {
            throw new InvalidRefreshTokenException();
        }
        try {
            UUID tokenId = UUID.fromString(raw.substring(0, dot));
            String secret = raw.substring(dot + 1);
            return new ParsedRefreshToken(tokenId, secret);
        } catch (IllegalArgumentException e) {
            throw new InvalidRefreshTokenException();
        }
    }

    private record ParsedRefreshToken(UUID tokenId, String secret) {
    }
}
