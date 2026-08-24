package com.coopshield.soc.eventnormalization.infrastructure.kafka;

import com.coopshield.soc.eventnormalization.application.NormalizedEventPublisher;
import com.coopshield.soc.sharedkernel.event.EventEnvelope;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica o evento normalizado, serializado como JSON, em
 * {@code security.normalized-events}. A chave da mensagem e o
 * {@code correlationId} (nao o {@code eventId}): o topico tem multiplas
 * particoes, e o motor de deteccao (Fase 6) correlaciona eventos do mesmo
 * ator/janela de tempo - eles precisam chegar na mesma particao, na ordem
 * em que aconteceram, o que o Kafka so garante dentro de uma particao.
 */
@Component
public class KafkaNormalizedEventPublisher implements NormalizedEventPublisher {

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaNormalizedEventPublisher(KafkaTemplate<Object, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(EventEnvelope event) {
        try {
            String payload = objectMapper.writeValueAsString(NormalizedEventMessage.from(event));
            kafkaTemplate.send(EventTopics.NORMALIZED_EVENTS, event.correlationId().toString(), payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize normalized event " + event.eventId(), e);
        }
    }
}
