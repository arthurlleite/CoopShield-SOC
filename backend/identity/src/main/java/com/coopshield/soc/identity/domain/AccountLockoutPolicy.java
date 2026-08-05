package com.coopshield.soc.identity.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Regra de bloqueio temporario apos tentativas repetidas de autenticacao,
 * conforme docs/product/vision.md (RF-18) e o catalogo de regras de
 * deteccao (RULE-001). Mantida separada de {@link User} para que o limite
 * de tentativas e a duracao do bloqueio sejam configuraveis sem alterar a
 * entidade.
 */
public class AccountLockoutPolicy {

    private final int maxFailedAttempts;
    private final Duration lockoutDuration;

    public AccountLockoutPolicy(int maxFailedAttempts, Duration lockoutDuration) {
        if (maxFailedAttempts <= 0) {
            throw new IllegalArgumentException("maxFailedAttempts must be positive");
        }
        this.maxFailedAttempts = maxFailedAttempts;
        this.lockoutDuration = Objects.requireNonNull(lockoutDuration, "lockoutDuration must not be null");
    }

    /**
     * Aplica o efeito de uma falha de autenticacao: incrementa o contador
     * do usuario e, ao atingir o limite, bloqueia a conta a partir de
     * {@code now} pela duracao configurada.
     */
    public void registerFailedAttempt(User user, Instant now) {
        user.recordFailedLogin();
        if (user.failedLoginAttempts() >= maxFailedAttempts) {
            user.lockUntil(now.plus(lockoutDuration));
        }
    }

    public void registerSuccessfulAttempt(User user) {
        user.recordSuccessfulLogin();
    }

    public int maxFailedAttempts() {
        return maxFailedAttempts;
    }

    public Duration lockoutDuration() {
        return lockoutDuration;
    }
}
