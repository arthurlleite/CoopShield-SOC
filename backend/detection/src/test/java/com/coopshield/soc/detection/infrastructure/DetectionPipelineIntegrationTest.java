package com.coopshield.soc.detection.infrastructure;

import com.coopshield.soc.detection.infrastructure.kafka.DetectionTopics;
import com.coopshield.soc.detection.infrastructure.kafka.NormalizedEventMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Prova o motor de deteccao contra Kafka e MongoDB reais: publica um evento
 * normalizado sintetico em {@code security.normalized-events} e observa
 * {@code detection_matches} (MongoDB) e {@code security.detection-alerts}
 * (Kafka) - ver docs/adr/ADR-014-motor-de-deteccao.md.
 */
@SpringBootTest
@Testcontainers
class DetectionPipelineIntegrationTest {

    @Container
    @ServiceConnection
    static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:7");

    // Ver docs/adr/ADR-013-pipeline-ingestao-normalizacao.md sobre por que se
    // usa a imagem Confluent aqui em vez de apache/kafka (usado no
    // docker-compose de execucao).
    @Container
    @ServiceConnection
    static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.1"));

    @Autowired
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    private KafkaConsumer<String, String> testConsumer;

    @BeforeEach
    void setUpConsumer() {
        Map<String, Object> props = KafkaTestUtils.consumerProps(
                KAFKA_CONTAINER.getBootstrapServers(), "test-observer-" + UUID.randomUUID(), "true");
        testConsumer = new KafkaConsumer<>(props, new StringDeserializer(), new StringDeserializer());
        testConsumer.subscribe(List.of(DetectionTopics.DETECTION_ALERTS));
        testConsumer.poll(Duration.ofMillis(500));
    }

    @AfterEach
    void tearDownConsumer() {
        testConsumer.close();
    }

    @Test
    void sensitiveExposureEventProducesAPersistedAndPublishedMatch() throws Exception {
        String eventId = UUID.randomUUID().toString();
        String correlationId = UUID.randomUUID().toString();

        NormalizedEventMessage message = new NormalizedEventMessage(
                eventId, "1.0", "data.access.sensitive.exposure", Instant.now(), "test-source",
                new NormalizedEventMessage.Actor("synthetic-user-01", "EMPLOYEE", "synthetic-unit"),
                new NormalizedEventMessage.Target("customer-record", "synthetic-record-01"),
                "QUERY", "FAILURE",
                new NormalizedEventMessage.Device("synthetic-device-01", true),
                new NormalizedEventMessage.NetworkContext("hashed-ip", "synthetic-region"),
                "INTERNAL", correlationId, Map.of());

        kafkaTemplate.send(DetectionTopics.NORMALIZED_EVENTS, eventId, objectMapper.writeValueAsString(message));

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
                assertThat(mongoTemplate.exists(
                        Query.query(Criteria.where("correlationId").is(correlationId)), "detection_matches"))
                        .isTrue());

        org.bson.Document persisted = mongoTemplate.findOne(
                Query.query(Criteria.where("correlationId").is(correlationId)), org.bson.Document.class, "detection_matches");
        assertThat(persisted).isNotNull();
        assertThat(persisted.getString("ruleId")).isEqualTo("RULE-010");
        assertThat(persisted.getString("severity")).isEqualTo("CRITICAL");

        ConsumerRecord<String, String> published = findRecord(correlationId);
        assertThat(published).isNotNull();
        assertThat(published.value()).contains("RULE-010");
    }

    private ConsumerRecord<String, String> findRecord(String correlationId) {
        long deadline = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < deadline) {
            var records = testConsumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, String> record : records) {
                if (record.value().contains(correlationId)) {
                    return record;
                }
            }
        }
        return null;
    }
}
