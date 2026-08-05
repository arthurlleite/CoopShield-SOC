package com.coopshield.soc.sharedkernel.event;

import com.coopshield.soc.sharedkernel.identifiers.CorrelationId;
import com.coopshield.soc.sharedkernel.identifiers.EventId;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Envelope normalizado de um evento de seguranca sintetico, conforme
 * definido em docs/event-catalog/events.md. Instancias sao imutaveis: uma
 * correcao gera um novo evento com {@code eventVersion} incrementada, nunca
 * a alteracao de uma instancia existente.
 */
public record EventEnvelope(
        EventId eventId,
        String eventVersion,
        String eventType,
        Instant timestamp,
        String source,
        Actor actor,
        Target target,
        String action,
        Outcome outcome,
        DeviceContext device,
        NetworkContext networkContext,
        DataClassification dataClassification,
        CorrelationId correlationId,
        Map<String, String> metadata
) {

    public EventEnvelope {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(eventVersion, "eventVersion must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(actor, "actor must not be null");
        Objects.requireNonNull(target, "target must not be null");
        Objects.requireNonNull(action, "action must not be null");
        Objects.requireNonNull(outcome, "outcome must not be null");
        Objects.requireNonNull(device, "device must not be null");
        Objects.requireNonNull(networkContext, "networkContext must not be null");
        Objects.requireNonNull(dataClassification, "dataClassification must not be null");
        Objects.requireNonNull(correlationId, "correlationId must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");

        if (eventType.isBlank()) {
            throw new IllegalArgumentException("eventType must not be blank");
        }
        if (action.isBlank()) {
            throw new IllegalArgumentException("action must not be blank");
        }

        metadata = Map.copyOf(metadata);
    }
}
