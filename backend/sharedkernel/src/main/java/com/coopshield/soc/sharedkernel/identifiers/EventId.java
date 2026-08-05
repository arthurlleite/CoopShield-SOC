package com.coopshield.soc.sharedkernel.identifiers;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador unico e imutavel de um evento de seguranca (sintetico).
 */
public record EventId(UUID value) {

    public EventId {
        Objects.requireNonNull(value, "EventId value must not be null");
    }

    public static EventId newId() {
        return new EventId(UUID.randomUUID());
    }

    public static EventId of(String value) {
        Objects.requireNonNull(value, "value must not be null");
        return new EventId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
