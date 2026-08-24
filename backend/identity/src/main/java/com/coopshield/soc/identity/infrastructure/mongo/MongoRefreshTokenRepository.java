package com.coopshield.soc.identity.infrastructure.mongo;

import com.coopshield.soc.identity.application.RefreshTokenRepository;
import com.coopshield.soc.identity.domain.RefreshToken;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador MongoDB de {@link RefreshTokenRepository} (colecao
 * {@code refresh_tokens}), com indice TTL em {@code expiresAt} para
 * expurgo automatico (ver docs/adr/ADR-012-mongodb-real-fase-3.md).
 */
@Component
public class MongoRefreshTokenRepository implements RefreshTokenRepository {

    private final SpringDataRefreshTokenMongoRepository springDataRepository;

    public MongoRefreshTokenRepository(SpringDataRefreshTokenMongoRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public void save(RefreshToken token) {
        Objects.requireNonNull(token, "token must not be null");
        springDataRepository.save(RefreshTokenDocument.fromDomain(token));
    }

    @Override
    public Optional<RefreshToken> findById(UUID tokenId) {
        return springDataRepository.findById(tokenId.toString()).map(RefreshTokenDocument::toDomain);
    }
}
