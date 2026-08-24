package com.coopshield.soc.identity.infrastructure.mongo;

import com.coopshield.soc.identity.domain.RefreshToken;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

/**
 * Modelo de persistencia da colecao {@code refresh_tokens}. O indice TTL em
 * {@code expiresAt} remove automaticamente tokens expirados (revogados ou
 * nao) - ver docs/adr/ADR-012-mongodb-real-fase-3.md. Diferente de
 * {@code audit_logs}, que nao deve expirar automaticamente.
 */
@Document(collection = "refresh_tokens")
public class RefreshTokenDocument {

    @Id
    private String tokenId;

    @Indexed
    private String userId;

    private String secretHash;

    @Indexed(expireAfterSeconds = 0)
    private Instant expiresAt;

    private boolean revoked;

    protected RefreshTokenDocument() {
        // Construtor exigido pelo Spring Data para materializacao via reflexao.
    }

    public RefreshTokenDocument(String tokenId, String userId, String secretHash, Instant expiresAt, boolean revoked) {
        this.tokenId = tokenId;
        this.userId = userId;
        this.secretHash = secretHash;
        this.expiresAt = expiresAt;
        this.revoked = revoked;
    }

    public static RefreshTokenDocument fromDomain(RefreshToken token) {
        return new RefreshTokenDocument(
                token.tokenId().toString(),
                token.userId().toString(),
                token.secretHash(),
                token.expiresAt(),
                token.revoked());
    }

    public RefreshToken toDomain() {
        return RefreshToken.rehydrate(
                UUID.fromString(tokenId), UUID.fromString(userId), secretHash, expiresAt, revoked);
    }

    public String getTokenId() {
        return tokenId;
    }

    public String getUserId() {
        return userId;
    }

    public String getSecretHash() {
        return secretHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }
}
