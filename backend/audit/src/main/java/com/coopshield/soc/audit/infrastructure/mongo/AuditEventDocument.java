package com.coopshield.soc.audit.infrastructure.mongo;

import com.coopshield.soc.audit.domain.AuditEvent;
import com.coopshield.soc.audit.domain.AuditEventType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Modelo de persistencia da colecao {@code audit_logs}. Sem indice TTL:
 * diferente de {@code refresh_tokens}, a trilha de auditoria nao deve
 * expirar automaticamente (ver docs/adr/ADR-012-mongodb-real-fase-3.md).
 */
@Document(collection = "audit_logs")
public class AuditEventDocument {

    @Id
    private String eventId;

    @Indexed
    private AuditEventType eventType;

    @Indexed
    private String actor;

    @Indexed
    private Instant timestamp;

    private Map<String, String> details;

    protected AuditEventDocument() {
        // Construtor exigido pelo Spring Data para materializacao via reflexao.
    }

    public AuditEventDocument(String eventId, AuditEventType eventType, String actor,
                              Instant timestamp, Map<String, String> details) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.actor = actor;
        this.timestamp = timestamp;
        this.details = details;
    }

    public static AuditEventDocument fromDomain(AuditEvent event) {
        return new AuditEventDocument(
                event.eventId().toString(), event.eventType(), event.actor(), event.timestamp(), event.details());
    }

    public AuditEvent toDomain() {
        return new AuditEvent(UUID.fromString(eventId), eventType, actor, timestamp, details);
    }

    public String getEventId() {
        return eventId;
    }

    public AuditEventType getEventType() {
        return eventType;
    }

    public String getActor() {
        return actor;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Map<String, String> getDetails() {
        return details;
    }
}
