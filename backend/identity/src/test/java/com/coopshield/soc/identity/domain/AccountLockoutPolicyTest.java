package com.coopshield.soc.identity.domain;

import com.coopshield.soc.sharedkernel.identity.Role;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AccountLockoutPolicyTest {

    private static final Instant NOW = Instant.parse("2026-08-05T10:00:00Z");

    private User aUser() {
        return new User(UUID.randomUUID(), "synthetic-analyst-01", "hashed-value", Role.SOC_ANALYST, true);
    }

    @Test
    void doesNotLockBeforeReachingMaxAttempts() {
        AccountLockoutPolicy policy = new AccountLockoutPolicy(5, Duration.ofMinutes(15));
        User user = aUser();

        for (int i = 0; i < 4; i++) {
            policy.registerFailedAttempt(user, NOW);
        }

        assertThat(user.isLockedAt(NOW)).isFalse();
        assertThat(user.failedLoginAttempts()).isEqualTo(4);
    }

    @Test
    void locksAccountOnReachingMaxAttempts() {
        AccountLockoutPolicy policy = new AccountLockoutPolicy(5, Duration.ofMinutes(15));
        User user = aUser();

        for (int i = 0; i < 5; i++) {
            policy.registerFailedAttempt(user, NOW);
        }

        assertThat(user.isLockedAt(NOW)).isTrue();
        assertThat(user.isLockedAt(NOW.plus(Duration.ofMinutes(15)).plusSeconds(1))).isFalse();
    }

    @Test
    void successfulAttemptClearsLockState() {
        AccountLockoutPolicy policy = new AccountLockoutPolicy(3, Duration.ofMinutes(15));
        User user = aUser();
        policy.registerFailedAttempt(user, NOW);
        policy.registerFailedAttempt(user, NOW);

        policy.registerSuccessfulAttempt(user);

        assertThat(user.failedLoginAttempts()).isZero();
        assertThat(user.isLockedAt(NOW)).isFalse();
    }

    @Test
    void rejectsNonPositiveMaxAttempts() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new AccountLockoutPolicy(0, Duration.ofMinutes(15)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
