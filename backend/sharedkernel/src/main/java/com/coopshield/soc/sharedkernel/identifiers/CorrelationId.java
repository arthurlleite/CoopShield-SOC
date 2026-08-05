package com.coopshield.soc.sharedkernel.identifiers;

import java.util.Objects;
import java.util.UUID;

/**
 * Identificador que conecta eventos pertencentes a uma mesma jornada
 * (ex.: uma sessao, uma investigacao), permitindo rastreabilidade ponta a
 * ponta entre ingestao, deteccao, alerta e incidente.
 */
public record CorrelationId(UUID value) {

    public CorrelationId {
        Objects.requireNonNull(value, "CorrelationId value must not be null");
    }

    public static CorrelationId newId() {
        return new CorrelationId(UUID.randomUUID());
    }

    public static CorrelationId of(String value) {
        Objects.requireNonNull(value, "value must not be null");
        return new CorrelationId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
