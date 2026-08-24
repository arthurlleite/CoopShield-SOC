package com.coopshield.soc.eventnormalization.infrastructure.mongo;

import com.coopshield.soc.sharedkernel.event.Actor;
import com.coopshield.soc.sharedkernel.event.DataClassification;
import com.coopshield.soc.sharedkernel.event.DeviceContext;
import com.coopshield.soc.sharedkernel.event.EventEnvelope;
import com.coopshield.soc.sharedkernel.event.NetworkContext;
import com.coopshield.soc.sharedkernel.event.Outcome;
import com.coopshield.soc.sharedkernel.event.Target;
import com.coopshield.soc.sharedkernel.identifiers.CorrelationId;
import com.coopshield.soc.sharedkernel.identifiers.EventId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

/**
 * Modelo de persistencia da colecao {@code security_events} - ver
 * docs/adr/ADR-012-mongodb-real-fase-3.md (que reservou esta colecao para a
 * Fase 4) e docs/adr/ADR-013-pipeline-ingestao-normalizacao.md. O indice
 * unico em {@code eventId} e o mecanismo de idempotencia: uma tentativa de
 * inserir um evento ja processado falha com chave duplicada, que
 * {@code MongoNormalizedEventRepository} trata como reprocessamento, nao
 * como erro.
 */
@Document(collection = "security_events")
public class SecurityEventDocument {

    @Id
    private String eventId;

    private String eventVersion;

    @Indexed
    private String eventType;

    @Indexed
    private Instant timestamp;

    private String source;
    private String actorUserId;
    private String actorRole;
    private String actorUnit;
    private String targetResourceType;
    private String targetResourceId;
    private String action;
    private Outcome outcome;
    private String deviceId;
    private boolean deviceKnown;
    private String networkIpHash;
    private String networkGeo;
    private DataClassification dataClassification;

    @Indexed
    private String correlationId;

    private Map<String, String> metadata;

    protected SecurityEventDocument() {
        // Construtor exigido pelo Spring Data para materializacao via reflexao.
    }

    public SecurityEventDocument(
            String eventId, String eventVersion, String eventType, Instant timestamp, String source,
            String actorUserId, String actorRole, String actorUnit, String targetResourceType,
            String targetResourceId, String action, Outcome outcome, String deviceId, boolean deviceKnown,
            String networkIpHash, String networkGeo, DataClassification dataClassification, String correlationId,
            Map<String, String> metadata
    ) {
        this.eventId = eventId;
        this.eventVersion = eventVersion;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.source = source;
        this.actorUserId = actorUserId;
        this.actorRole = actorRole;
        this.actorUnit = actorUnit;
        this.targetResourceType = targetResourceType;
        this.targetResourceId = targetResourceId;
        this.action = action;
        this.outcome = outcome;
        this.deviceId = deviceId;
        this.deviceKnown = deviceKnown;
        this.networkIpHash = networkIpHash;
        this.networkGeo = networkGeo;
        this.dataClassification = dataClassification;
        this.correlationId = correlationId;
        this.metadata = metadata;
    }

    public static SecurityEventDocument fromDomain(EventEnvelope event) {
        return new SecurityEventDocument(
                event.eventId().toString(),
                event.eventVersion(),
                event.eventType(),
                event.timestamp(),
                event.source(),
                event.actor().userId(),
                event.actor().role(),
                event.actor().unit(),
                event.target().resourceType(),
                event.target().resourceId(),
                event.action(),
                event.outcome(),
                event.device().deviceId(),
                event.device().known(),
                event.networkContext().ipHash(),
                event.networkContext().geo(),
                event.dataClassification(),
                event.correlationId().toString(),
                event.metadata());
    }

    public EventEnvelope toDomain() {
        return new EventEnvelope(
                EventId.of(eventId),
                eventVersion,
                eventType,
                timestamp,
                source,
                new Actor(actorUserId, actorRole, actorUnit),
                new Target(targetResourceType, targetResourceId),
                action,
                outcome,
                new DeviceContext(deviceId, deviceKnown),
                new NetworkContext(networkIpHash, networkGeo),
                dataClassification,
                CorrelationId.of(correlationId),
                metadata);
    }

    public String getEventId() {
        return eventId;
    }
}
