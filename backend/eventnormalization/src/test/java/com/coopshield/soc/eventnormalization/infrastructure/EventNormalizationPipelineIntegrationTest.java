package com.coopshield.soc.eventnormalization.infrastructure;

import com.coopshield.soc.eventnormalization.infrastructure.kafka.EventTopics;
import com.coopshield.soc.eventnormalization.infrastructure.kafka.RawEventMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.bson.Document;
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
 * Prova o pipeline completo ingestao->normalizacao contra um Kafka e um
 * MongoDB reais (Testcontainers): publica em {@code security.raw-events} e
 * observa {@code security_events} (MongoDB) e {@code security.normalized-events}
 * (Kafka); reenvia o mesmo evento para provar idempotencia; publica uma
 * mensagem malformada e observa {@code security.dead-letter}. Ver
 * docs/adr/ADR-013-pipeline-ingestao-normalizacao.md.
 */
@SpringBootTest
@Testcontainers
class EventNormalizationPipelineIntegrationTest {

    @Container
    @ServiceConnection
    static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:7");

    // Imagem Confluent (nao apache/kafka, usado no docker-compose de execucao):
    // a classe nativa org.testcontainers.kafka.KafkaContainer com imagens
    // apache/kafka falha em detectar o advertised.listener correto neste
    // ambiente Docker Desktop/Windows ("advertised.listeners cannot use the
    // nonroutable meta-address 0.0.0.0"). org.testcontainers.containers.KafkaContainer
    // com confluentinc/cp-kafka e a combinacao mais testada do ecossistema
    // Testcontainers e nao apresenta esse problema; o protocolo Kafka exercitado
    // pelo teste e identico ao usado em producao.
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
        testConsumer.subscribe(List.of(EventTopics.NORMALIZED_EVENTS, EventTopics.DEAD_LETTER));
        testConsumer.poll(Duration.ofMillis(500));
    }

    @AfterEach
    void tearDownConsumer() {
        testConsumer.close();
    }

    @Test
    void normalizesRawEventPersistsItAndPublishesToNormalizedTopic() throws Exception {
        String eventId = UUID.randomUUID().toString();
        String correlationId = UUID.randomUUID().toString();
        String rawIp = "203.0.113.55";

        RawEventMessage raw = new RawEventMessage(
                eventId, "1.0", "authentication.login.failure", Instant.parse("2026-08-24T12:00:00Z"),
                "identity-service",
                new RawEventMessage.Actor("synthetic-user-01", "EMPLOYEE", "synthetic-branch-001"),
                new RawEventMessage.Target("account", "synthetic-account-01"),
                "LOGIN", "FAILURE",
                new RawEventMessage.Device("synthetic-device-01", false),
                rawIp, "synthetic-region", correlationId, Map.of());

        kafkaTemplate.send(EventTopics.RAW_EVENTS, eventId, objectMapper.writeValueAsString(raw));

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
                assertThat(mongoTemplate.exists(Query.query(Criteria.where("_id").is(eventId)), "security_events"))
                        .isTrue());

        Document persisted = mongoTemplate.findOne(
                Query.query(Criteria.where("_id").is(eventId)), Document.class, "security_events");
        assertThat(persisted).isNotNull();
        assertThat(persisted.toJson()).doesNotContain(rawIp);
        assertThat(persisted.getString("dataClassification")).isEqualTo("INTERNAL");
        assertThat(persisted.getString("networkIpHash")).isNotBlank();

        ConsumerRecord<String, String> normalized = findRecord(EventTopics.NORMALIZED_EVENTS, eventId);
        assertThat(normalized).isNotNull();
        assertThat(normalized.value()).doesNotContain(rawIp);
        assertThat(normalized.value()).contains("\"dataClassification\":\"INTERNAL\"");

        // Reenvio do mesmo evento (mesmo eventId): idempotencia - nenhum documento novo.
        kafkaTemplate.send(EventTopics.RAW_EVENTS, eventId, objectMapper.writeValueAsString(raw));
        Thread.sleep(3000);
        assertThat(mongoTemplate.count(Query.query(Criteria.where("_id").is(eventId)), "security_events"))
                .isEqualTo(1);
    }

    @Test
    void routesMalformedRawEventToDeadLetter() {
        String malformedEventId = UUID.randomUUID().toString();
        String malformedPayload = """
                {"eventId":"%s","eventVersion":"1.0","eventType":"authentication.login.failure",
                "timestamp":"2026-08-24T12:00:00Z","source":"identity-service",
                "actor":{"userId":"synthetic-user-01","role":"EMPLOYEE","unit":null},
                "target":{"resourceType":"account","resourceId":"synthetic-account-01"},
                "action":null,"outcome":"FAILURE",
                "device":{"deviceId":"synthetic-device-01","known":false},
                "sourceIp":"203.0.113.60","geo":null,"correlationId":"%s","metadata":{}}
                """.formatted(malformedEventId, UUID.randomUUID());

        kafkaTemplate.send(EventTopics.RAW_EVENTS, malformedEventId, malformedPayload);

        ConsumerRecord<String, String> deadLettered = findRecord(EventTopics.DEAD_LETTER, malformedEventId);
        assertThat(deadLettered).isNotNull();
        assertThat(deadLettered.value()).contains(malformedEventId);

        assertThat(mongoTemplate.exists(
                Query.query(Criteria.where("_id").is(malformedEventId)), "security_events")).isFalse();
    }

    private ConsumerRecord<String, String> findRecord(String topic, String eventId) {
        // A chave da mensagem publicada por eventnormalization e o
        // correlationId, nao o eventId (ver KafkaNormalizedEventPublisher) -
        // localizar pelo conteudo, nao pela chave.
        long deadline = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < deadline) {
            var records = testConsumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, String> record : records) {
                if (topic.equals(record.topic()) && record.value().contains(eventId)) {
                    return record;
                }
            }
        }
        return null;
    }

    /**
     * Este modulo isolado (sem {@code eventingestion}) nao tem o bean
     * {@code NewTopic} de {@code security.raw-events} (dono do topico e
     * eventingestion - ver ADR-013). Declarar o topico aqui garante que ele
     * exista, com a configuracao esperada, antes de qualquer container de
     * listener/producer o tocar, em vez de depender de auto-criacao
     * (correta por padrao, mas uma fonte extra de nao-determinismo evitavel
     * em teste). A causa raiz da corrida real observada nestes testes era
     * outra - o listener nao fixava {@code auto.offset.reset=earliest} por
     * conta propria (corrigido em {@link RawEventListener}) - mas provisionar
     * o topico explicitamente continua sendo a pratica correta.
     */
    @org.springframework.boot.test.context.TestConfiguration
    static class RawEventsTopicTestConfiguration {

        @org.springframework.context.annotation.Bean
        org.apache.kafka.clients.admin.NewTopic rawEventsTopic() {
            return org.springframework.kafka.config.TopicBuilder.name(EventTopics.RAW_EVENTS)
                    .partitions(3).replicas(1).build();
        }
    }
}
