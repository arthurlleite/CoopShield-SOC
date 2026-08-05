package com.coopshield.soc.identity.infrastructure;

import com.coopshield.soc.identity.application.RefreshTokenRepository;
import com.coopshield.soc.identity.domain.RefreshToken;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adaptador em memoria de {@link RefreshTokenRepository}. Sera
 * substituido por um adaptador MongoDB na Fase 3 (ver
 * docs/adr/ADR-011-persistencia-em-memoria-fase-2.md).
 */
@Component
public class InMemoryRefreshTokenRepository implements RefreshTokenRepository {

    private final Map<UUID, RefreshToken> byId = new ConcurrentHashMap<>();

    @Override
    public void save(RefreshToken token) {
        Objects.requireNonNull(token, "token must not be null");
        byId.put(token.tokenId(), token);
    }

    @Override
    public Optional<RefreshToken> findById(UUID tokenId) {
        return Optional.ofNullable(byId.get(tokenId));
    }
}
