package com.coopshield.soc.identity.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Refresh token opaco de longa duracao. O valor apresentado ao cliente e
 * composto por {@code tokenId} (usado como chave de busca, o "seletor")
 * mais um segredo aleatorio; apenas o hash do segredo e mantido aqui, para
 * que uma leitura do repositorio nao exponha um token utilizavel.
 */
public class RefreshToken {

    private final UUID tokenId;
    private final UUID userId;
    private final String secretHash;
    private final Instant expiresAt;
    private boolean revoked;

    public RefreshToken(UUID tokenId, UUID userId, String secretHash, Instant expiresAt) {
        this.tokenId = Objects.requireNonNull(tokenId, "tokenId must not be null");
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.secretHash = Objects.requireNonNull(secretHash, "secretHash must not be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt must not be null");
        this.revoked = false;
    }

    public boolean isValidAt(Instant now, String candidateSecretHash) {
        return !revoked && expiresAt.isAfter(now) && secretHash.equals(candidateSecretHash);
    }

    public void revoke() {
        this.revoked = true;
    }

    public UUID tokenId() {
        return tokenId;
    }

    public UUID userId() {
        return userId;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public boolean revoked() {
        return revoked;
    }
}
