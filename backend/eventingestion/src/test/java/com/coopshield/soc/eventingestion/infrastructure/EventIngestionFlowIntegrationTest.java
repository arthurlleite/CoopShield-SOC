package com.coopshield.soc.eventingestion.infrastructure;

import com.coopshield.soc.eventingestion.infrastructure.kafka.EventTopics;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova, de ponta a ponta dentro do modulo, que {@code POST /api/v1/events}
 * valida, aceita e publica o evento em {@code security.raw-events} contra um
 * Kafka real (Testcontainers).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class EventIngestionFlowIntegrationTest {

    // Ver EventNormalizationPipelineIntegrationTest sobre por que se usa a
    // imagem Confluent aqui em vez de apache/kafka (usado no docker-compose
    // de execucao).
    @Container
    @ServiceConnection
    static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.7.1"));

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private KafkaConsumer<String, String> testConsumer;

    @BeforeEach
    void setUpConsumer() {
        Map<String, Object> props = KafkaTestUtils.consumerProps(
                KAFKA_CONTAINER.getBootstrapServers(), "test-observer-" + UUID.randomUUID(), "true");
        testConsumer = new KafkaConsumer<>(props, new StringDeserializer(), new StringDeserializer());
        testConsumer.subscribe(List.of(EventTopics.RAW_EVENTS));
        testConsumer.poll(Duration.ofMillis(500));
    }

    @AfterEach
    void tearDownConsumer() {
        testConsumer.close();
    }

    @Test
    void acceptsValidEventAndPublishesItToRawEventsTopic() {
        Map<String, Object> request = Map.ofEntries(
                Map.entry("eventType", "authentication.login.failure"),
                Map.entry("source", "identity-service"),
                Map.entry("actorUserId", "synthetic-user-01"),
                Map.entry("actorRole", "EMPLOYEE"),
                Map.entry("targetResourceType", "account"),
                Map.entry("targetResourceId", "synthetic-account-01"),
                Map.entry("action", "LOGIN"),
                Map.entry("outcome", "failure"),
                Map.entry("deviceId", "synthetic-device-01"),
                Map.entry("deviceKnown", false),
                Map.entry("sourceIp", "203.0.113.77"));

        ResponseEntity<Map> response = restTemplate.postForEntity(url("/api/v1/events"), request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        String eventId = (String) response.getBody().get("eventId");
        assertThat(eventId).isNotBlank();

        ConsumerRecord<String, String> record = findRecord(eventId);
        assertThat(record).isNotNull();
        assertThat(record.value()).contains("203.0.113.77");
        assertThat(record.value()).contains(eventId);
    }

    @Test
    void rejectsEventWithMissingRequiredFieldsWithoutPublishing() {
        Map<String, Object> request = Map.of("eventType", "authentication.login.failure");

        ResponseEntity<Map> response = restTemplate.postForEntity(url("/api/v1/events"), request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat((List<?>) response.getBody().get("violations")).isNotEmpty();
    }

    @Test
    void rejectsEventWithInvalidOutcome() {
        Map<String, Object> request = Map.of(
                "eventType", "authentication.login.failure",
                "source", "identity-service",
                "actorUserId", "synthetic-user-01",
                "actorRole", "EMPLOYEE",
                "targetResourceType", "account",
                "targetResourceId", "synthetic-account-01",
                "action", "LOGIN",
                "outcome", "not-a-real-outcome",
                "deviceId", "synthetic-device-01",
                "sourceIp", "203.0.113.77");

        ResponseEntity<Map> response = restTemplate.postForEntity(url("/api/v1/events"), request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private ConsumerRecord<String, String> findRecord(String eventId) {
        // A chave da mensagem e o correlationId, nao o eventId (ver
        // KafkaRawEventPublisher) - localizar pelo conteudo, nao pela chave.
        long deadline = System.currentTimeMillis() + 40_000;
        while (System.currentTimeMillis() < deadline) {
            var records = testConsumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, String> record : records) {
                if (record.value().contains(eventId)) {
                    return record;
                }
            }
        }
        return null;
    }
}
