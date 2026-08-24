package com.coopshield.soc.simulation.domain;

import java.util.Map;
import java.util.Objects;

/**
 * Um evento bruto sintetico gerado por um passo de {@link ScenarioDefinition}.
 * Nao inclui ator (todos os eventos de uma execucao compartilham o mesmo
 * {@link Character}) nem identificadores/timestamp (atribuidos pela
 * ingestao, ver {@code EventIngestionService}).
 */
public record GeneratedEvent(
        String eventType,
        String action,
        String outcome,
        String targetResourceType,
        String targetResourceId,
        String deviceId,
        boolean deviceKnown,
        String sourceIp,
        String geo,
        Map<String, String> metadata
) {

    public GeneratedEvent {
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(action, "action must not be null");
        Objects.requireNonNull(outcome, "outcome must not be null");
        Objects.requireNonNull(targetResourceType, "targetResourceType must not be null");
        Objects.requireNonNull(targetResourceId, "targetResourceId must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(sourceIp, "sourceIp must not be null");
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
