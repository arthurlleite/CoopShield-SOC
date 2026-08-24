package com.coopshield.soc.identity.infrastructure.mongo;

import com.coopshield.soc.identity.domain.User;
import com.coopshield.soc.sharedkernel.identity.Role;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

/**
 * Modelo de persistencia da colecao {@code users}. Deliberadamente
 * separado da entidade de dominio {@link User}: o dominio nao conhece
 * anotacoes do Spring Data, e este documento nao carrega nenhuma regra de
 * negocio (ver docs/adr/ADR-009-arquitetura-hexagonal.md).
 */
@Document(collection = "users")
public class UserDocument {

    @Id
    private String userId;

    @Indexed(unique = true)
    private String username;

    private String passwordHash;
    private Role role;
    private boolean enabled;
    private int failedLoginAttempts;
    private Instant lockedUntil;

    protected UserDocument() {
        // Construtor exigido pelo Spring Data para materializacao via reflexao.
    }

    public UserDocument(String userId, String username, String passwordHash, Role role,
                        boolean enabled, int failedLoginAttempts, Instant lockedUntil) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
        this.failedLoginAttempts = failedLoginAttempts;
        this.lockedUntil = lockedUntil;
    }

    public static UserDocument fromDomain(User user) {
        return new UserDocument(
                user.userId().toString(),
                user.username(),
                user.passwordHash(),
                user.role(),
                user.enabled(),
                user.failedLoginAttempts(),
                user.lockedUntil());
    }

    public User toDomain() {
        return User.rehydrate(UUID.fromString(userId), username, passwordHash, role, enabled,
                failedLoginAttempts, lockedUntil);
    }

    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Instant getLockedUntil() {
        return lockedUntil;
    }
}
