package com.coopshield.soc.eventingestion.infrastructure.kafka;

import com.coopshield.soc.eventingestion.domain.RawEvent;

import java.time.Instant;
import java.util.Map;

/**
 * Forma de serializacao (JSON) de {@link RawEvent} publicada no topico
 * {@code security.raw-events}. Deliberadamente separada do record de
 * dominio para nao acoplar o dominio a anotacoes/formato de serializacao
 * (ver ADR-009). Difere do envelope normalizado
 * ({@link com.coopshield.soc.sharedkernel.event.EventEnvelope}) por trazer
 * {@code sourceIp} em vez de {@code networkContext.ipHash}, e por nao
 * carregar {@code dataClassification}.
 */
public record RawEventMessage(
        String eventId,
        String eventVersion,
        String eventType,
        Instant timestamp,
        String source,
        Actor actor,
        Target target,
        String action,
        String outcome,
        Device device,
        String sourceIp,
        String geo,
        String correlationId,
        Map<String, String> metadata
) {

    public static RawEventMessage from(RawEvent event) {
        return new RawEventMessage(
                event.eventId().toString(),
                event.eventVersion(),
                event.eventType(),
                event.timestamp(),
                event.source(),
                new Actor(event.actorUserId(), event.actorRole(), event.actorUnit()),
                new Target(event.targetResourceType(), event.targetResourceId()),
                event.action(),
                event.outcome().name(),
                new Device(event.deviceId(), event.deviceKnown()),
                event.sourceIp(),
                event.geo(),
                event.correlationId().toString(),
                event.metadata());
    }

    public record Actor(String userId, String role, String unit) {
    }

    public record Target(String resourceType, String resourceId) {
    }

    public record Device(String deviceId, boolean known) {
    }
}
