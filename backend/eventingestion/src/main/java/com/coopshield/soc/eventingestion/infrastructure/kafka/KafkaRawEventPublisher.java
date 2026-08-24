package com.coopshield.soc.eventingestion.infrastructure.kafka;

import com.coopshield.soc.eventingestion.application.RawEventPublisher;
import com.coopshield.soc.eventingestion.domain.RawEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publica o evento bruto validado, serializado como JSON
 * ({@link RawEventMessage}), em {@code security.raw-events}. A chave da
 * mensagem e o {@code eventId}, garantindo que reenvios do mesmo evento
 * (ex.: retry do cliente) caiam na mesma particao.
 */
@Component
public class KafkaRawEventPublisher implements RawEventPublisher {

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaRawEventPublisher(KafkaTemplate<Object, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(RawEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(RawEventMessage.from(event));
            kafkaTemplate.send(EventTopics.RAW_EVENTS, event.eventId().toString(), payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize raw event " + event.eventId(), e);
        }
    }
}
