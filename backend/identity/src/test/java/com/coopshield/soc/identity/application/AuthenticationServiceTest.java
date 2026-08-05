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
import com.coopshield.soc.sharedkernel.identity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private AccessTokenIssuer accessTokenIssuer;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuditPort auditPort;

    private AuthenticationService service;

    @BeforeEach
    void setUp() {
        AccountLockoutPolicy lockoutPolicy = new AccountLockoutPolicy(3, Duration.ofMinutes(15));
        service = new AuthenticationService(
                userRepository, refreshTokenRepository, accessTokenIssuer, passwordEncoder,
                lockoutPolicy, auditPort, Duration.ofDays(7));
    }

    private User aUser() {
        return new User(UUID.randomUUID(), "synthetic-analyst-01", "hashed-password", Role.SOC_ANALYST, true);
    }

    @Test
    void loginSucceedsAndIssuesTokens() {
        User user = aUser();
        when(userRepository.findByUsername("synthetic-analyst-01")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("correct-password", user.passwordHash())).thenReturn(true);
        when(accessTokenIssuer.issue(user.userId(), user.username(), user.role()))
                .thenReturn(new AccessToken("jwt-value", Instant.now().plusSeconds(900)));

        AuthenticationResult result = service.login("synthetic-analyst-01", "correct-password");

        assertThat(result.accessToken()).isEqualTo("jwt-value");
        assertThat(result.refreshToken()).contains(".");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(auditPort).record(argThatType(AuditEventType.AUTHENTICATION_SUCCESS));
    }

    @Test
    void loginFailsForUnknownUser() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login("ghost", "whatever"))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(auditPort).record(argThatType(AuditEventType.AUTHENTICATION_FAILURE));
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void loginFailsForDisabledUser() {
        User disabled = new User(UUID.randomUUID(), "synthetic-disabled-01", "hashed-password", Role.EMPLOYEE, false);
        when(userRepository.findByUsername("synthetic-disabled-01")).thenReturn(Optional.of(disabled));

        assertThatThrownBy(() -> service.login("synthetic-disabled-01", "whatever"))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void loginFailsForWrongPasswordAndIncrementsAttempts() {
        User user = aUser();
        when(userRepository.findByUsername("synthetic-analyst-01")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", user.passwordHash())).thenReturn(false);

        assertThatThrownBy(() -> service.login("synthetic-analyst-01", "wrong-password"))
                .isInstanceOf(InvalidCredentialsException.class);

        assertThat(user.failedLoginAttempts()).isEqualTo(1);
        verify(userRepository).save(user);
        verify(auditPort).record(argThatType(AuditEventType.AUTHENTICATION_FAILURE));
    }

    @Test
    void accountLocksAfterMaxFailedAttemptsAndRejectsEvenCorrectPassword() {
        User user = aUser();
        when(userRepository.findByUsername("synthetic-analyst-01")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(eq("wrong-password"), any())).thenReturn(false);

        for (int i = 0; i < 3; i++) {
            assertThatThrownBy(() -> service.login("synthetic-analyst-01", "wrong-password"))
                    .isInstanceOf(InvalidCredentialsException.class);
        }
        assertThat(user.isLockedAt(Instant.now())).isTrue();

        // mesmo com a senha correta, a conta permanece bloqueada
        assertThatThrownBy(() -> service.login("synthetic-analyst-01", "correct-password"))
                .isInstanceOf(AccountLockedException.class);

        verify(passwordEncoder, never()).matches(eq("correct-password"), any());
        verify(auditPort).record(argThatType(AuditEventType.ACCOUNT_LOCKED));
    }

    @Test
    void refreshRotatesTokenAndIssuesNewPair() {
        User user = aUser();
        when(accessTokenIssuer.issue(any(), any(), any()))
                .thenReturn(new AccessToken("first-jwt", Instant.now().plusSeconds(900)))
                .thenReturn(new AccessToken("second-jwt", Instant.now().plusSeconds(900)));
        when(userRepository.findByUsername(user.username())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        // captura o refresh token emitido no login para reutilizar no refresh
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        AuthenticationResult loginResult = service.login(user.username(), "any-password");
        verify(refreshTokenRepository).save(captor.capture());
        RefreshToken issued = captor.getValue();

        when(refreshTokenRepository.findById(issued.tokenId())).thenReturn(Optional.of(issued));
        when(userRepository.findById(user.userId())).thenReturn(Optional.of(user));

        AuthenticationResult refreshed = service.refresh(loginResult.refreshToken());

        assertThat(refreshed.accessToken()).isEqualTo("second-jwt");
        assertThat(issued.revoked()).isTrue();
        verify(auditPort).record(argThatType(AuditEventType.TOKEN_REFRESHED));
    }

    @Test
    void refreshFailsForMalformedToken() {
        assertThatThrownBy(() -> service.refresh("not-a-valid-token"))
                .isInstanceOf(InvalidRefreshTokenException.class);
        verify(refreshTokenRepository, never()).findById(any());
    }

    @Test
    void refreshFailsForUnknownTokenId() {
        UUID tokenId = UUID.randomUUID();
        when(refreshTokenRepository.findById(tokenId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh(tokenId + ".some-secret"))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void refreshFailsForExpiredToken() {
        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String secret = "a-secret-value";
        RefreshToken expired = new RefreshToken(tokenId, userId, RefreshTokenSecretHasher.hash(secret), Instant.now().minusSeconds(1));
        when(refreshTokenRepository.findById(tokenId)).thenReturn(Optional.of(expired));

        assertThatThrownBy(() -> service.refresh(tokenId + "." + secret))
                .isInstanceOf(InvalidRefreshTokenException.class);
    }

    @Test
    void logoutRevokesKnownToken() {
        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        String secret = "a-secret-value";
        RefreshToken token = new RefreshToken(tokenId, userId, RefreshTokenSecretHasher.hash(secret), Instant.now().plusSeconds(60));
        when(refreshTokenRepository.findById(tokenId)).thenReturn(Optional.of(token));

        service.logout(tokenId + "." + secret);

        assertThat(token.revoked()).isTrue();
        verify(refreshTokenRepository, times(1)).save(token);
        verify(auditPort).record(argThatType(AuditEventType.LOGOUT));
    }

    @Test
    void logoutIsANoOpForGarbageInput() {
        service.logout("not-a-valid-token");

        verify(refreshTokenRepository, never()).save(any());
    }

    private AuditEvent argThatType(AuditEventType type) {
        return org.mockito.ArgumentMatchers.argThat(event -> event != null && event.eventType() == type);
    }
}
