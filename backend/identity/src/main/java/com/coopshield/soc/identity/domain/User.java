package com.coopshield.soc.identity.domain;

import com.coopshield.soc.sharedkernel.identity.Role;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Usuario sintetico do CoopShield SOC. Mantem o estado necessario para
 * autenticacao e bloqueio temporario; a politica de quando bloquear
 * pertence a {@link AccountLockoutPolicy}, nao a esta entidade.
 */
public class User {

    private final UUID userId;
    private final String username;
    private String passwordHash;
    private final Role role;
    private final boolean enabled;
    private int failedLoginAttempts;
    private Instant lockedUntil;

    public User(UUID userId, String username, String passwordHash, Role role, boolean enabled) {
        this.userId = Objects.requireNonNull(userId, "userId must not be null");
        this.username = requireNonBlank(username, "username");
        this.passwordHash = requireNonBlank(passwordHash, "passwordHash");
        this.role = Objects.requireNonNull(role, "role must not be null");
        this.enabled = enabled;
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    private static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    public boolean isLockedAt(Instant now) {
        return lockedUntil != null && lockedUntil.isAfter(now);
    }

    void recordSuccessfulLogin() {
        this.failedLoginAttempts = 0;
        this.lockedUntil = null;
    }

    void recordFailedLogin() {
        this.failedLoginAttempts++;
    }

    void lockUntil(Instant until) {
        this.lockedUntil = Objects.requireNonNull(until, "until must not be null");
    }

    public UUID userId() {
        return userId;
    }

    public String username() {
        return username;
    }

    public String passwordHash() {
        return passwordHash;
    }

    public Role role() {
        return role;
    }

    public boolean enabled() {
        return enabled;
    }

    public int failedLoginAttempts() {
        return failedLoginAttempts;
    }

    public Instant lockedUntil() {
        return lockedUntil;
    }
}
