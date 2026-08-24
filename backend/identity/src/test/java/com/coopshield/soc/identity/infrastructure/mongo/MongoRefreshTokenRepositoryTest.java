package com.coopshield.soc.identity.infrastructure.mongo;

import com.coopshield.soc.identity.domain.RefreshToken;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de persistencia real contra um MongoDB efemero (Testcontainers)
 * para {@link MongoRefreshTokenRepository} - ver
 * docs/adr/ADR-012-mongodb-real-fase-3.md.
 */
@DataMongoTest
@Testcontainers
class MongoRefreshTokenRepositoryTest {

    @Container
    @ServiceConnection
    static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:7");

    @Autowired
    private SpringDataRefreshTokenMongoRepository springDataRepository;

    private MongoRefreshTokenRepository repository;

    @Test
    void savesAndFindsById() {
        repository = new MongoRefreshTokenRepository(springDataRepository);
        RefreshToken token = new RefreshToken(UUID.randomUUID(), UUID.randomUUID(), "hash-value", Instant.now().plusSeconds(60));

        repository.save(token);

        RefreshToken reloaded = repository.findById(token.tokenId()).orElseThrow();
        assertThat(reloaded.userId()).isEqualTo(token.userId());
        assertThat(reloaded.secretHash()).isEqualTo("hash-value");
        assertThat(reloaded.revoked()).isFalse();
    }

    @Test
    void persistsRevocation() {
        repository = new MongoRefreshTokenRepository(springDataRepository);
        RefreshToken token = new RefreshToken(UUID.randomUUID(), UUID.randomUUID(), "hash-value", Instant.now().plusSeconds(60));
        repository.save(token);

        token.revoke();
        repository.save(token);

        RefreshToken reloaded = repository.findById(token.tokenId()).orElseThrow();
        assertThat(reloaded.revoked()).isTrue();
    }

    @Test
    void returnsEmptyForUnknownToken() {
        repository = new MongoRefreshTokenRepository(springDataRepository);

        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }
}
