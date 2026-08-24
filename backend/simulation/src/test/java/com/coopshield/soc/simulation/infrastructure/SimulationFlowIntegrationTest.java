package com.coopshield.soc.simulation.infrastructure;

import com.coopshield.soc.eventingestion.infrastructure.kafka.EventTopics;
import com.coopshield.soc.simulation.domain.Characters;
import com.coopshield.soc.simulation.domain.Scenario;
import com.coopshield.soc.simulation.infrastructure.web.CharacterResponse;
import com.coopshield.soc.simulation.infrastructure.web.RunResponse;
import com.coopshield.soc.simulation.infrastructure.web.ScenarioResponse;
import com.coopshield.soc.simulation.infrastructure.web.StartRunRequest;
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
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova, dentro do modulo, que iniciar uma execucao do laboratorio publica
 * os eventos gerados em {@code security.raw-events} atraves do mesmo
 * caminho de {@code EventIngestionService} usado pela API publica.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class SimulationFlowIntegrationTest {

    @Container
    @ServiceConnection
    static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:7");

    // Ver docs/adr/ADR-013-pipeline-ingestao-normalizacao.md sobre por que se
    // usa a imagem Confluent aqui em vez de apache/kafka (usado no
    // docker-compose de execucao).
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
    void listsAllScenariosAndCharacters() {
        ResponseEntity<ScenarioResponse[]> scenarios =
                restTemplate.getForEntity(url("/api/v1/simulation/scenarios"), ScenarioResponse[].class);
        ResponseEntity<CharacterResponse[]> characters =
                restTemplate.getForEntity(url("/api/v1/simulation/characters"), CharacterResponse[].class);

        assertThat(scenarios.getBody()).hasSize(12);
        assertThat(characters.getBody()).hasSize(6);
    }

    @Test
    void startingARunPublishesEveryGeneratedEventToRawEvents() {
        StartRunRequest request = new StartRunRequest(Scenario.UNKNOWN_DEVICE.id(), Characters.PATRICIA_GOMES.id(), 3);

        ResponseEntity<RunResponse> response = restTemplate.postForEntity(url("/api/v1/simulation/runs"), request, RunResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        RunResponse run = response.getBody();
        assertThat(run.status()).isEqualTo("COMPLETED");
        assertThat(run.publishedEventCount()).isEqualTo(3);

        List<ConsumerRecord<String, String>> matching = findRecordsByCorrelationId(run.correlationId(), 3);
        assertThat(matching).hasSize(3);

        ResponseEntity<RunResponse> fetched = restTemplate.getForEntity(url("/api/v1/simulation/runs/" + run.runId()), RunResponse.class);
        assertThat(fetched.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(fetched.getBody().runId()).isEqualTo(run.runId());
    }

    @Test
    void rejectsUnknownScenarioWithBadRequest() {
        StartRunRequest request = new StartRunRequest("not-a-scenario", Characters.ANA_BEATRIZ.id(), 2);

        ResponseEntity<Map> response = restTemplate.postForEntity(url("/api/v1/simulation/runs"), request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private List<ConsumerRecord<String, String>> findRecordsByCorrelationId(String correlationId, int expectedCount) {
        List<ConsumerRecord<String, String>> matching = new java.util.ArrayList<>();
        long deadline = System.currentTimeMillis() + 30_000;
        while (System.currentTimeMillis() < deadline && matching.size() < expectedCount) {
            var records = testConsumer.poll(Duration.ofMillis(500));
            for (ConsumerRecord<String, String> record : records) {
                if (record.value().contains(correlationId)) {
                    matching.add(record);
                }
            }
        }
        return matching;
    }
}
