package com.coopshield.soc.eventingestion.application;

import com.coopshield.soc.eventingestion.domain.RawEvent;
import com.coopshield.soc.eventingestion.domain.RawEventValidationException;
import com.coopshield.soc.sharedkernel.event.Outcome;
import com.coopshield.soc.sharedkernel.identifiers.CorrelationId;
import com.coopshield.soc.sharedkernel.identifiers.EventId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Valida um evento bruto recebido, aplica os valores padrao dos campos
 * opcionais (eventId, eventVersion, timestamp, correlationId, metadata) e
 * publica no topico {@code security.raw-events} via {@link RawEventPublisher}.
 */
public class EventIngestionService {

    private static final String DEFAULT_EVENT_VERSION = "1.0";

    private final RawEventPublisher publisher;

    public EventIngestionService(RawEventPublisher publisher) {
        this.publisher = publisher;
    }

    public RawEvent ingest(
            String eventId, String eventVersion, String eventType, Instant timestamp, String source,
            String actorUserId, String actorRole, String actorUnit, String targetResourceType,
            String targetResourceId, String action, String outcome, String deviceId, boolean deviceKnown,
            String sourceIp, String geo, String correlationId, Map<String, String> metadata
    ) {
        List<String> violations = new ArrayList<>();
        Outcome parsedOutcome = parseOutcome(outcome, violations);
        EventId parsedEventId = parseEventId(eventId, violations);
        CorrelationId parsedCorrelationId = parseCorrelationId(correlationId, violations);
        if (!violations.isEmpty()) {
            throw new RawEventValidationException(violations);
        }

        RawEvent event = new RawEvent(
                parsedEventId,
                eventVersion == null || eventVersion.isBlank() ? DEFAULT_EVENT_VERSION : eventVersion,
                eventType,
                timestamp == null ? Instant.now() : timestamp,
                source,
                actorUserId,
                actorRole,
                actorUnit,
                targetResourceType,
                targetResourceId,
                action,
                parsedOutcome,
                deviceId,
                deviceKnown,
                sourceIp,
                geo,
                parsedCorrelationId,
                metadata == null ? Map.of() : metadata
        );

        publisher.publish(event);
        return event;
    }

    private Outcome parseOutcome(String outcome, List<String> violations) {
        try {
            return Outcome.valueOf(outcome.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            violations.add("outcome must be one of " + List.of(Outcome.values()) + " but was '" + outcome + "'");
            return null;
        }
    }

    private EventId parseEventId(String eventId, List<String> violations) {
        if (eventId == null || eventId.isBlank()) {
            return EventId.newId();
        }
        try {
            return EventId.of(eventId);
        } catch (IllegalArgumentException e) {
            violations.add("eventId must be a valid UUID but was '" + eventId + "'");
            return null;
        }
    }

    private CorrelationId parseCorrelationId(String correlationId, List<String> violations) {
        if (correlationId == null || correlationId.isBlank()) {
            return CorrelationId.newId();
        }
        try {
            return CorrelationId.of(correlationId);
        } catch (IllegalArgumentException e) {
            violations.add("correlationId must be a valid UUID but was '" + correlationId + "'");
            return null;
        }
    }
}
