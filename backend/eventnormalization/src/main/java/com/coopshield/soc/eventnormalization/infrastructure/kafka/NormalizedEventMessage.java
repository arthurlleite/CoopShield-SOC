package com.coopshield.soc.eventnormalization.infrastructure.kafka;

import com.coopshield.soc.sharedkernel.event.EventEnvelope;

import java.time.Instant;
import java.util.Map;

/**
 * Forma de serializacao (JSON) de {@link EventEnvelope}, publicada em
 * {@code security.normalized-events}. Deliberadamente separada do record de
 * dominio: {@code sharedkernel} nao depende de frameworks externos (ver seu
 * pom.xml), logo nao pode carregar anotacoes Jackson, e sem elas
 * {@code EventId}/{@code CorrelationId} seriam serializados como objetos
 * aninhados ({@code {"value":"uuid"}}) em vez do formato de string plano do
 * catalogo de eventos.
 */
public record NormalizedEventMessage(
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
        NetworkContext networkContext,
        String dataClassification,
        String correlationId,
        Map<String, String> metadata
) {

    public static NormalizedEventMessage from(EventEnvelope event) {
        return new NormalizedEventMessage(
                event.eventId().toString(),
                event.eventVersion(),
                event.eventType(),
                event.timestamp(),
                event.source(),
                new Actor(event.actor().userId(), event.actor().role(), event.actor().unit()),
                new Target(event.target().resourceType(), event.target().resourceId()),
                event.action(),
                event.outcome().name(),
                new Device(event.device().deviceId(), event.device().known()),
                new NetworkContext(event.networkContext().ipHash(), event.networkContext().geo()),
                event.dataClassification().name(),
                event.correlationId().toString(),
                event.metadata());
    }

    public record Actor(String userId, String role, String unit) {
    }

    public record Target(String resourceType, String resourceId) {
    }

    public record Device(String deviceId, boolean known) {
    }

    public record NetworkContext(String ipHash, String geo) {
    }
}
