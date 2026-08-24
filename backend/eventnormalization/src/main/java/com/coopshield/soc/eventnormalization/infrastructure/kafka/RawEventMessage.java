package com.coopshield.soc.eventnormalization.infrastructure.kafka;

import java.time.Instant;
import java.util.Map;

/**
 * Forma de deserializacao (JSON) de uma mensagem recebida em
 * {@code security.raw-events}, espelhando o formato publicado por
 * {@code eventingestion} (RawEventMessage). Ver
 * docs/adr/ADR-013-pipeline-ingestao-normalizacao.md sobre por que este
 * tipo nao e compartilhado entre os dois modulos.
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

    public record Actor(String userId, String role, String unit) {
    }

    public record Target(String resourceType, String resourceId) {
    }

    public record Device(String deviceId, boolean known) {
    }
}
