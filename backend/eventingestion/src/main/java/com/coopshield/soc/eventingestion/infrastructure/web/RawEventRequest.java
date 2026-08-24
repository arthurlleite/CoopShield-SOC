package com.coopshield.soc.eventingestion.infrastructure.web;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;
import java.util.Map;

/**
 * Corpo de requisicao aceito por {@code POST /api/v1/events}. Campos
 * ausentes/opcionais (eventId, timestamp, correlationId, eventVersion,
 * actorUnit, geo, metadata) recebem valores padrao em
 * {@code EventIngestionService}; os demais sao obrigatorios e validados
 * aqui.
 */
public record RawEventRequest(
        String eventId,
        String eventVersion,
        @NotBlank String eventType,
        Instant timestamp,
        @NotBlank String source,
        @NotBlank String actorUserId,
        @NotBlank String actorRole,
        String actorUnit,
        @NotBlank String targetResourceType,
        @NotBlank String targetResourceId,
        @NotBlank String action,
        @NotBlank String outcome,
        @NotBlank String deviceId,
        boolean deviceKnown,
        @NotBlank String sourceIp,
        String geo,
        String correlationId,
        Map<String, String> metadata
) {
}
