package com.coopshield.soc.identity.infrastructure;

import com.coopshield.soc.identity.domain.RefreshToken;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryRefreshTokenRepositoryTest {

    @Test
    void savesAndFindsById() {
        InMemoryRefreshTokenRepository repository = new InMemoryRefreshTokenRepository();
        RefreshToken token = new RefreshToken(UUID.randomUUID(), UUID.randomUUID(), "hash", Instant.now().plusSeconds(60));

        repository.save(token);

        assertThat(repository.findById(token.tokenId())).contains(token);
    }

    @Test
    void returnsEmptyForUnknownToken() {
        InMemoryRefreshTokenRepository repository = new InMemoryRefreshTokenRepository();

        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }
}
