package com.coopshield.soc.simulation.domain;

import java.util.Objects;

/**
 * Personagem sintetico do laboratorio de simulacao, correspondendo a uma
 * das personas descritas em docs/product/personas-use-cases.md. Carrega os
 * mesmos campos usados por {@link com.coopshield.soc.sharedkernel.event.Actor}
 * ao publicar eventos gerados por este personagem.
 */
public record Character(String id, String displayName, String userId, String role, String unit) {

    public Character {
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(displayName, "displayName must not be null");
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(role, "role must not be null");
        if (id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
    }
}
