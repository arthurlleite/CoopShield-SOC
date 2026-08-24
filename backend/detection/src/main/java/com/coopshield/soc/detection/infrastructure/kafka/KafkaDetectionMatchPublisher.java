package com.coopshield.soc.detection.infrastructure.kafka;

import com.coopshield.soc.detection.application.DetectionMatchPublisher;
import com.coopshield.soc.detection.domain.DetectionMatch;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaDetectionMatchPublisher implements DetectionMatchPublisher {

    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public KafkaDetectionMatchPublisher(KafkaTemplate<Object, Object> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(DetectionMatch match) {
        try {
            String payload = objectMapper.writeValueAsString(DetectionMatchMessage.from(match));
            kafkaTemplate.send(DetectionTopics.DETECTION_ALERTS, match.matchId().toString(), payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize detection match " + match.matchId(), e);
        }
    }
}
