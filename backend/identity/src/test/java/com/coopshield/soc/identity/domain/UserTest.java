package com.coopshield.soc.identity.domain;

import com.coopshield.soc.sharedkernel.identity.Role;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    private User aUser() {
        return new User(UUID.randomUUID(), "synthetic-analyst-01", "hashed-value", Role.SOC_ANALYST, true);
    }

    @Test
    void isNotLockedByDefault() {
        User user = aUser();

        assertThat(user.isLockedAt(Instant.now())).isFalse();
    }

    @Test
    void isLockedWhileLockedUntilIsInTheFuture() {
        User user = aUser();
        Instant now = Instant.parse("2026-08-05T10:00:00Z");
        user.lockUntil(now.plusSeconds(60));

        assertThat(user.isLockedAt(now)).isTrue();
        assertThat(user.isLockedAt(now.plusSeconds(61))).isFalse();
    }

    @Test
    void successfulLoginResetsFailedAttemptsAndUnlocks() {
        User user = aUser();
        user.recordFailedLogin();
        user.recordFailedLogin();
        user.lockUntil(Instant.now().plusSeconds(60));

        user.recordSuccessfulLogin();

        assertThat(user.failedLoginAttempts()).isZero();
        assertThat(user.isLockedAt(Instant.now())).isFalse();
    }

    @Test
    void rejectsBlankUsername() {
        assertThatThrownBy(() -> new User(UUID.randomUUID(), " ", "hash", Role.EMPLOYEE, true))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
