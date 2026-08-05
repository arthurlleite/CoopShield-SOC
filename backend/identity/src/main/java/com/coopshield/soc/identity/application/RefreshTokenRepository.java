package com.coopshield.soc.identity.application;

import com.coopshield.soc.identity.domain.RefreshToken;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saida para persistencia de refresh tokens. Implementada em
 * memoria na Fase 2 (ver docs/adr/ADR-011-persistencia-em-memoria-fase-2.md).
 */
public interface RefreshTokenRepository {

    void save(RefreshToken token);

    Optional<RefreshToken> findById(UUID tokenId);
}
