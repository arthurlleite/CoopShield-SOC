package com.coopshield.soc.detection.infrastructure.kafka;

import com.coopshield.soc.detection.application.DetectionEngine;
import com.coopshield.soc.sharedkernel.event.Actor;
import com.coopshield.soc.sharedkernel.event.DataClassification;
import com.coopshield.soc.sharedkernel.event.DeviceContext;
import com.coopshield.soc.sharedkernel.event.EventEnvelope;
import com.coopshield.soc.sharedkernel.event.NetworkContext;
import com.coopshield.soc.sharedkernel.event.Outcome;
import com.coopshield.soc.sharedkernel.event.Target;
import com.coopshield.soc.sharedkernel.identifiers.CorrelationId;
import com.coopshield.soc.sharedkernel.identifiers.EventId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consome {@code security.normalized-events} e delega a
 * {@link DetectionEngine}. Mensagens malformadas propagam a excecao de
 * parsing para o error handler padrao do container (nao configuramos um
 * dead-letter proprio aqui: uma mensagem que ja passou pela normalizacao da
 * Fase 4 e, por construcao, sempre valida - um erro de parsing aqui indica
 * um bug de contrato entre os dois modulos, nao um dado de entrada
 * inesperado).
 */
@Component
public class NormalizedEventListener {

    private final DetectionEngine detectionEngine;
    private final ObjectMapper objectMapper;

    public NormalizedEventListener(DetectionEngine detectionEngine, ObjectMapper objectMapper) {
        this.detectionEngine = detectionEngine;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = DetectionTopics.NORMALIZED_EVENTS,
            groupId = "detection",
            properties = "auto.offset.reset=earliest")
    public void onNormalizedEvent(String payload) throws Exception {
        NormalizedEventMessage message = objectMapper.readValue(payload, NormalizedEventMessage.class);
        detectionEngine.evaluate(toEnvelope(message));
    }

    private EventEnvelope toEnvelope(NormalizedEventMessage message) {
        return new EventEnvelope(
                EventId.of(message.eventId()),
                message.eventVersion(),
                message.eventType(),
                message.timestamp(),
                message.source(),
                new Actor(message.actor().userId(), message.actor().role(), message.actor().unit()),
                new Target(message.target().resourceType(), message.target().resourceId()),
                message.action(),
                Outcome.valueOf(message.outcome()),
                new DeviceContext(message.device().deviceId(), message.device().known()),
                new NetworkContext(message.networkContext().ipHash(), message.networkContext().geo()),
                DataClassification.valueOf(message.dataClassification()),
                CorrelationId.of(message.correlationId()),
                message.metadata());
    }
}
