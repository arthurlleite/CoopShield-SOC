package com.coopshield.soc.identity.infrastructure.mongo;

import com.coopshield.soc.identity.application.UserRepository;
import com.coopshield.soc.identity.domain.User;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador MongoDB de {@link UserRepository} (colecao {@code users}).
 * Substitui o adaptador em memoria da Fase 2 (ver
 * docs/adr/ADR-011-persistencia-em-memoria-fase-2.md e
 * docs/adr/ADR-012-mongodb-real-fase-3.md); a porta e os consumidores
 * (AuthenticationService, testes de unidade com mocks) nao mudaram.
 */
@Component
public class MongoUserRepository implements UserRepository {

    private final SpringDataUserMongoRepository springDataRepository;

    public MongoUserRepository(SpringDataUserMongoRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return springDataRepository.findByUsername(username).map(UserDocument::toDomain);
    }

    @Override
    public Optional<User> findById(UUID userId) {
        return springDataRepository.findById(userId.toString()).map(UserDocument::toDomain);
    }

    @Override
    public void save(User user) {
        Objects.requireNonNull(user, "user must not be null");
        springDataRepository.save(UserDocument.fromDomain(user));
    }
}
