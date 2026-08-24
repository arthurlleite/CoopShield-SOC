package com.coopshield.soc.eventingestion.domain;

import com.coopshield.soc.sharedkernel.event.Outcome;
import com.coopshield.soc.sharedkernel.identifiers.CorrelationId;
import com.coopshield.soc.sharedkernel.identifiers.EventId;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Evento sintetico bruto, validado na borda de ingestao mas ainda nao
 * normalizado: ao contrario de {@link com.coopshield.soc.sharedkernel.event.EventEnvelope},
 * carrega o endereco IP de origem em texto puro ({@code sourceIp}), porque
 * o hash (exigido por {@link com.coopshield.soc.sharedkernel.event.NetworkContext})
 * so e calculado pelo modulo {@code eventnormalization} - ver
 * docs/adr/ADR-013-pipeline-ingestao-normalizacao.md. Nao carrega
 * {@code dataClassification}: essa classificacao e responsabilidade do
 * modulo {@code dataprotection} (Fase 9), nao da ingestao.
 */
public record RawEvent(
        EventId eventId,
        String eventVersion,
        String eventType,
        Instant timestamp,
        String source,
        String actorUserId,
        String actorRole,
        String actorUnit,
        String targetResourceType,
        String targetResourceId,
        String action,
        Outcome outcome,
        String deviceId,
        boolean deviceKnown,
        String sourceIp,
        String geo,
        CorrelationId correlationId,
        Map<String, String> metadata
) {

    public RawEvent {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(eventVersion, "eventVersion must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(actorUserId, "actorUserId must not be null");
        Objects.requireNonNull(actorRole, "actorRole must not be null");
        Objects.requireNonNull(targetResourceType, "targetResourceType must not be null");
        Objects.requireNonNull(targetResourceId, "targetResourceId must not be null");
        Objects.requireNonNull(action, "action must not be null");
        Objects.requireNonNull(outcome, "outcome must not be null");
        Objects.requireNonNull(deviceId, "deviceId must not be null");
        Objects.requireNonNull(sourceIp, "sourceIp must not be null");
        Objects.requireNonNull(correlationId, "correlationId must not be null");
        Objects.requireNonNull(metadata, "metadata must not be null");
        metadata = Map.copyOf(metadata);
    }
}
