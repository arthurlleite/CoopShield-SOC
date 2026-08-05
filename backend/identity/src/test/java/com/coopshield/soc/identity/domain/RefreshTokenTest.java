package com.coopshield.soc.identity.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RefreshTokenTest {

    private static final Instant NOW = Instant.parse("2026-08-05T10:00:00Z");

    @Test
    void isValidWhenNotExpiredNotRevokedAndSecretMatches() {
        RefreshToken token = new RefreshToken(UUID.randomUUID(), UUID.randomUUID(), "secret-hash", NOW.plusSeconds(60));

        assertThat(token.isValidAt(NOW, "secret-hash")).isTrue();
    }

    @Test
    void isInvalidWhenSecretDoesNotMatch() {
        RefreshToken token = new RefreshToken(UUID.randomUUID(), UUID.randomUUID(), "secret-hash", NOW.plusSeconds(60));

        assertThat(token.isValidAt(NOW, "wrong-hash")).isFalse();
    }

    @Test
    void isInvalidWhenExpired() {
        RefreshToken token = new RefreshToken(UUID.randomUUID(), UUID.randomUUID(), "secret-hash", NOW.minusSeconds(1));

        assertThat(token.isValidAt(NOW, "secret-hash")).isFalse();
    }

    @Test
    void isInvalidAfterRevocation() {
        RefreshToken token = new RefreshToken(UUID.randomUUID(), UUID.randomUUID(), "secret-hash", NOW.plusSeconds(60));

        token.revoke();

        assertThat(token.isValidAt(NOW, "secret-hash")).isFalse();
        assertThat(token.revoked()).isTrue();
    }
}
