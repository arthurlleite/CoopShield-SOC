package com.coopshield.soc.eventnormalization.application;

import com.coopshield.soc.eventnormalization.domain.EventNormalizationException;
import com.coopshield.soc.sharedkernel.event.Actor;
import com.coopshield.soc.sharedkernel.event.DataClassification;
import com.coopshield.soc.sharedkernel.event.DeviceContext;
import com.coopshield.soc.sharedkernel.event.EventEnvelope;
import com.coopshield.soc.sharedkernel.event.NetworkContext;
import com.coopshield.soc.sharedkernel.event.Outcome;
import com.coopshield.soc.sharedkernel.event.Target;
import com.coopshield.soc.sharedkernel.identifiers.CorrelationId;
import com.coopshield.soc.sharedkernel.identifiers.EventId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Transforma um evento bruto recebido de {@code security.raw-events} em um
 * {@link EventEnvelope} valido, persiste de forma idempotente e publica em
 * {@code security.normalized-events}.
 *
 * <p><b>Classificacao provisoria:</b> {@code dataClassification} e
 * responsabilidade definitiva do modulo {@code dataprotection} (Fase 9,
 * ainda nao implementado). Ate la, todo evento normalizado recebe
 * {@link DataClassification#INTERNAL} como valor conservador padrao - ver
 * docs/adr/ADR-013-pipeline-ingestao-normalizacao.md.
 */
public class EventNormalizationService {

    private final NormalizedEventRepository repository;
    private final NormalizedEventPublisher publisher;

    public EventNormalizationService(NormalizedEventRepository repository, NormalizedEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public void normalize(
            String eventId, String eventVersion, String eventType, Instant timestamp, String source,
            String actorUserId, String actorRole, String actorUnit, String targetResourceType,
            String targetResourceId, String action, String outcome, String deviceId, boolean deviceKnown,
            String sourceIp, String geo, String correlationId, Map<String, String> metadata
    ) {
        List<String> violations = new ArrayList<>();

        EventId parsedEventId = require(eventId, "eventId", violations, EventId::of);
        CorrelationId parsedCorrelationId = require(correlationId, "correlationId", violations, CorrelationId::of);
        Outcome parsedOutcome = require(outcome, "outcome", violations, value -> Outcome.valueOf(value.trim().toUpperCase()));
        requireBlank(eventType, "eventType", violations);
        requireBlank(source, "source", violations);
        requireBlank(actorUserId, "actor.userId", violations);
        requireBlank(actorRole, "actor.role", violations);
        requireBlank(targetResourceType, "target.resourceType", violations);
        requireBlank(targetResourceId, "target.resourceId", violations);
        requireBlank(action, "action", violations);
        requireBlank(deviceId, "device.deviceId", violations);
        requireBlank(sourceIp, "sourceIp", violations);
        if (timestamp == null) {
            violations.add("timestamp must not be null");
        }

        if (!violations.isEmpty()) {
            throw new EventNormalizationException(violations);
        }

        EventEnvelope envelope = new EventEnvelope(
                parsedEventId,
                eventVersion == null || eventVersion.isBlank() ? "1.0" : eventVersion,
                eventType,
                timestamp,
                source,
                new Actor(actorUserId, actorRole, actorUnit),
                new Target(targetResourceType, targetResourceId),
                action,
                parsedOutcome,
                new DeviceContext(deviceId, deviceKnown),
                new NetworkContext(IpHasher.hash(sourceIp), geo == null || geo.isBlank() ? "unknown" : geo),
                DataClassification.INTERNAL,
                parsedCorrelationId,
                metadata == null ? Map.of() : metadata);

        boolean newlyPersisted = repository.saveIfAbsent(envelope);
        if (newlyPersisted) {
            publisher.publish(envelope);
        }
    }

    private void requireBlank(String value, String fieldName, List<String> violations) {
        if (value == null || value.isBlank()) {
            violations.add(fieldName + " must not be blank");
        }
    }

    private <T> T require(String value, String fieldName, List<String> violations, java.util.function.Function<String, T> parser) {
        if (value == null || value.isBlank()) {
            violations.add(fieldName + " must not be blank");
            return null;
        }
        try {
            return parser.apply(value);
        } catch (IllegalArgumentException e) {
            violations.add(fieldName + " is invalid: '" + value + "'");
            return null;
        }
    }
}
