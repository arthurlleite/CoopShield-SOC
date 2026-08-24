package com.coopshield.soc.app;

import com.coopshield.soc.identity.infrastructure.web.TokenResponse;
import com.coopshield.soc.simulation.domain.Characters;
import com.coopshield.soc.simulation.domain.Scenario;
import com.coopshield.soc.simulation.infrastructure.web.RunResponse;
import com.coopshield.soc.simulation.infrastructure.web.StartRunRequest;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Prova a cadeia completa ponta a ponta: simulacao -> ingestao -> Kafka ->
 * normalizacao -> Kafka -> deteccao, atraves da API HTTP real. Executa o
 * cenario de referencia da Fase 0 ("conta possivelmente comprometida") e
 * confirma que RULE-001 (falhas seguidas de sucesso) e RULE-003 (dispositivo
 * desconhecido) sao ambas acionadas - a mesma dupla de regras da jornada
 * descrita em docs/product/personas-use-cases.md.
 */
class DetectionEndToEndIntegrationTest extends AbstractIntegrationTest {

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
    void accountCompromisedScenarioTriggersFailureThenSuccessAndUnknownDeviceRules() {
        TokenResponse tokens = login("synthetic-soc-analyst-01").getBody();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokens.accessToken());

        StartRunRequest request = new StartRunRequest(Scenario.ACCOUNT_COMPROMISED.id(), Characters.FERNANDA_LIMA.id(), 6);
        ResponseEntity<RunResponse> response = restTemplate.postForEntity(
                url("/api/v1/simulation/runs"), new HttpEntity<>(request, headers), RunResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        String correlationId = response.getBody().correlationId();

        await().atMost(Duration.ofSeconds(25)).untilAsserted(() -> {
            List<org.bson.Document> matches = mongoTemplate.find(
                    Query.query(Criteria.where("correlationId").is(correlationId)),
                    org.bson.Document.class, "detection_matches");
            Set<String> ruleIds = matches.stream().map(doc -> doc.getString("ruleId")).collect(Collectors.toSet());
            assertThat(ruleIds).contains("RULE-001", "RULE-003");
        });
    }

    private ResponseEntity<TokenResponse> login(String username) {
        return restTemplate.postForEntity(
                url("/api/v1/auth/login"), Map.of("username", username, "password", SYNTHETIC_PASSWORD),
                TokenResponse.class);
    }
}
