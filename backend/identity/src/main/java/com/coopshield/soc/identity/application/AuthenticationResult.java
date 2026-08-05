package com.coopshield.soc.identity.application;

import java.time.Instant;
import java.util.Objects;

/**
 * Resultado de uma autenticacao ou renovacao bem-sucedida, pronto para ser
 * traduzido pela camada de infraestrutura (REST) em uma resposta HTTP.
 */
public record AuthenticationResult(
        String accessToken,
        Instant accessTokenExpiresAt,
        String refreshToken,
        Instant refreshTokenExpiresAt
) {

    public AuthenticationResult {
        Objects.requireNonNull(accessToken, "accessToken must not be null");
        Objects.requireNonNull(accessTokenExpiresAt, "accessTokenExpiresAt must not be null");
        Objects.requireNonNull(refreshToken, "refreshToken must not be null");
        Objects.requireNonNull(refreshTokenExpiresAt, "refreshTokenExpiresAt must not be null");
    }
}
