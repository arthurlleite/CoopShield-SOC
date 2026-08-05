package com.coopshield.soc.sharedkernel.event;

import java.util.Objects;

/**
 * Ator (sintetico) responsavel por uma acao registrada em um evento.
 *
 * @param userId identificador sintetico do usuario/sistema que originou o evento
 * @param role   perfil do ator no momento do evento (ex.: SOC_ANALYST, EMPLOYEE)
 * @param unit   unidade ficticia associada ao ator (ex.: agencia sintetica)
 */
public record Actor(String userId, String role, String unit) {

    public Actor {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(role, "role must not be null");
        if (userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (role.isBlank()) {
            throw new IllegalArgumentException("role must not be blank");
        }
    }
}
