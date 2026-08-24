package com.coopshield.soc.identity.infrastructure.mongo;

import com.coopshield.soc.identity.domain.User;
import com.coopshield.soc.sharedkernel.identity.Role;
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
 * Testes de persistencia real contra um MongoDB efemero (Testcontainers),
 * provando que {@link MongoUserRepository} traduz corretamente entre
 * {@link User} (dominio) e {@link UserDocument} (persistencia) - ver
 * docs/adr/ADR-012-mongodb-real-fase-3.md.
 */
@DataMongoTest
@Testcontainers
class MongoUserRepositoryTest {

    @Container
    @ServiceConnection
    static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:7");

    @Autowired
    private SpringDataUserMongoRepository springDataRepository;

    private MongoUserRepository repository;

    @Test
    void savesAndFindsByUsernameAndById() {
        repository = new MongoUserRepository(springDataRepository);
        User user = new User(UUID.randomUUID(), "synthetic-analyst-01", "hashed-value", Role.SOC_ANALYST, true);

        repository.save(user);

        User byUsername = repository.findByUsername("synthetic-analyst-01").orElseThrow();
        User byId = repository.findById(user.userId()).orElseThrow();

        assertThat(byUsername.userId()).isEqualTo(user.userId());
        assertThat(byUsername.role()).isEqualTo(Role.SOC_ANALYST);
        assertThat(byId.username()).isEqualTo("synthetic-analyst-01");
    }

    @Test
    void persistsLockoutStateAcrossSaves() {
        repository = new MongoUserRepository(springDataRepository);
        User user = new User(UUID.randomUUID(), "synthetic-branch-manager-01", "hashed-value", Role.BRANCH_MANAGER, true);
        repository.save(user);

        User reloaded = repository.findById(user.userId()).orElseThrow();
        Instant lockUntil = Instant.now().plusSeconds(900);
        // Simula o efeito de AccountLockoutPolicy diretamente no estado persistido,
        // reidratando com o mesmo mecanismo usado pelo adaptador.
        User locked = User.rehydrate(reloaded.userId(), reloaded.username(), reloaded.passwordHash(),
                reloaded.role(), reloaded.enabled(), 5, lockUntil);
        repository.save(locked);

        User afterLock = repository.findById(user.userId()).orElseThrow();
        assertThat(afterLock.failedLoginAttempts()).isEqualTo(5);
        assertThat(afterLock.isLockedAt(Instant.now())).isTrue();
    }

    @Test
    void returnsEmptyForUnknownUser() {
        repository = new MongoUserRepository(springDataRepository);

        assertThat(repository.findByUsername("ghost")).isEmpty();
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }
}
