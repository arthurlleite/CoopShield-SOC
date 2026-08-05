package com.coopshield.soc.audit.domain;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Registro imutavel de auditoria. Nunca deve conter senhas, hashes de
 * senha, tokens, chaves ou dados sensiveis nao mascarados em
 * {@code details} - essa regra e reforcada por revisao de codigo e pelos
 * testes de ausencia de dado sensivel em log/auditoria.
 *
 * @param eventId   identificador unico do evento de auditoria
 * @param eventType tipo do evento
 * @param actor     usuario/sistema que originou a acao auditada (nunca nulo; use "anonymous" quando nao houver ator autenticado)
 * @param timestamp instante em que o evento ocorreu
 * @param details   metadados adicionais nao sensiveis (ex.: motivo, contagem de tentativas)
 */
public record AuditEvent(
        UUID eventId,
        AuditEventType eventType,
        String actor,
        Instant timestamp,
        Map<String, String> details
) {

    public AuditEvent {
        Objects.requireNonNull(eventId, "eventId must not be null");
        Objects.requireNonNull(eventType, "eventType must not be null");
        Objects.requireNonNull(actor, "actor must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        Objects.requireNonNull(details, "details must not be null");
        details = Map.copyOf(details);
    }

    public static AuditEvent of(AuditEventType eventType, String actor, Map<String, String> details) {
        return new AuditEvent(UUID.randomUUID(), eventType, actor, Instant.now(), details);
    }
}
