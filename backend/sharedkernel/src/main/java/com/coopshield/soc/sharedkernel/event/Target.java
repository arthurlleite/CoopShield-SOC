package com.coopshield.soc.sharedkernel.event;

import java.util.Objects;

/**
 * Recurso (sintetico) alvo de uma acao registrada em um evento.
 *
 * @param resourceType tipo do recurso (ex.: account, api-endpoint, permission)
 * @param resourceId   identificador sintetico do recurso
 */
public record Target(String resourceType, String resourceId) {

    public Target {
        Objects.requireNonNull(resourceType, "resourceType must not be null");
        Objects.requireNonNull(resourceId, "resourceId must not be null");
        if (resourceType.isBlank()) {
            throw new IllegalArgumentException("resourceType must not be blank");
        }
        if (resourceId.isBlank()) {
            throw new IllegalArgumentException("resourceId must not be blank");
        }
    }
}
