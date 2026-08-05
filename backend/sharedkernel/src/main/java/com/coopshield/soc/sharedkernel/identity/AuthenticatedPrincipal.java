package com.coopshield.soc.sharedkernel.identity;

import java.util.Objects;
import java.util.UUID;

/**
 * Identidade autenticada extraida de um token de acesso valido. E o
 * contrato usado pelo modulo accesscontrol para popular o contexto de
 * seguranca, sem depender da implementacao de token (JWT) do modulo
 * identity.
 *
 * @param userId   identificador sintetico do usuario
 * @param username nome de usuario sintetico
 * @param role     perfil do usuario no momento em que o token foi emitido
 */
public record AuthenticatedPrincipal(UUID userId, String username, Role role) {

    public AuthenticatedPrincipal {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(username, "username must not be null");
        Objects.requireNonNull(role, "role must not be null");
    }
}
