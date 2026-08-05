package com.coopshield.soc.identity.application;

import java.time.Instant;
import java.util.Objects;

/**
 * Access token de curta duracao emitido para um usuario autenticado.
 */
public record AccessToken(String value, Instant expiresAt) {

    public AccessToken {
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(expiresAt, "expiresAt must not be null");
    }
}
