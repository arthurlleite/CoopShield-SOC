package com.coopshield.soc.detection.infrastructure.kafka;

import java.time.Instant;
import java.util.Map;

/**
 * Forma de deserializacao (JSON) de uma mensagem recebida em
 * {@code security.normalized-events}, espelhando o formato publicado por
 * {@code eventnormalization} (NormalizedEventMessage). Nao compartilhado
 * como dependencia de modulo - mesmo raciocinio de
 * docs/adr/ADR-013-pipeline-ingestao-normalizacao.md aplicado a este
 * consumidor.
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

    public record Actor(String userId, String role, String unit) {
    }

    public record Target(String resourceType, String resourceId) {
    }

    public record Device(String deviceId, boolean known) {
    }

    public record NetworkContext(String ipHash, String geo) {
    }
}
