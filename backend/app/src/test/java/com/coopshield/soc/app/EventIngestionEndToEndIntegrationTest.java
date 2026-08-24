package com.coopshield.soc.app;

import com.coopshield.soc.identity.infrastructure.web.TokenResponse;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Prova o pipeline completo ponta a ponta atraves da API HTTP real: login,
 * {@code POST /api/v1/events} autenticado, e o evento chegando normalizado
 * (sem IP em texto puro) na colecao {@code security_events} apos passar por
 * Kafka - ver docs/adr/ADR-013-pipeline-ingestao-normalizacao.md. A
 * granularidade do pipeline Kafka em si (dead-letter, idempotencia) e
 * coberta pelos testes dos modulos eventingestion/eventnormalization; este
 * teste prova a integracao real entre a API autenticada e o pipeline.
 */
class EventIngestionEndToEndIntegrationTest extends AbstractIntegrationTest {

    private static final String SYNTHETIC_PASSWORD = "Synthetic#Pass123";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void authenticatedUserSubmitsEventAndItArrivesNormalizedInMongo() {
        TokenResponse tokens = login("synthetic-soc-analyst-01").getBody();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokens.accessToken());

        Map<String, Object> request = Map.ofEntries(
                Map.entry("eventType", "device.unrecognized"),
                Map.entry("source", "eventingestion-e2e-test"),
                Map.entry("actorUserId", "synthetic-user-e2e"),
                Map.entry("actorRole", "EMPLOYEE"),
                Map.entry("targetResourceType", "account"),
                Map.entry("targetResourceId", "synthetic-account-e2e"),
                Map.entry("action", "LOGIN"),
                Map.entry("outcome", "SUCCESS"),
                Map.entry("deviceId", "synthetic-device-e2e"),
                Map.entry("deviceKnown", false),
                Map.entry("sourceIp", "198.51.100.42"));

        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/events"), new HttpEntity<>(request, headers), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        String eventId = (String) response.getBody().get("eventId");

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
                assertThat(mongoTemplate.exists(Query.query(Criteria.where("_id").is(eventId)), "security_events"))
                        .isTrue());

        Document persisted = mongoTemplate.findOne(
                Query.query(Criteria.where("_id").is(eventId)), Document.class, "security_events");
        assertThat(persisted).isNotNull();
        assertThat(persisted.toJson()).doesNotContain("198.51.100.42");
        assertThat(persisted.getString("dataClassification")).isEqualTo("INTERNAL");
    }

    @Test
    void unauthenticatedRequestIsRejected() {
        Map<String, Object> request = Map.of("eventType", "device.unrecognized");

        ResponseEntity<Map> response = restTemplate.postForEntity(url("/api/v1/events"), request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private ResponseEntity<TokenResponse> login(String username) {
        return restTemplate.postForEntity(
                url("/api/v1/auth/login"), Map.of("username", username, "password", SYNTHETIC_PASSWORD),
                TokenResponse.class);
    }
}
