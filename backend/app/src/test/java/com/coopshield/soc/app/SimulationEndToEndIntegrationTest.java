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
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Prova o laboratorio de simulacao de ponta a ponta atraves da API HTTP
 * real: login, {@code POST /api/v1/simulation/runs} para o cenario "conta
 * possivelmente comprometida" (jornada de referencia da Fase 0), e todos os
 * eventos gerados chegando normalizados em MongoDB, ligados pelo mesmo
 * {@code correlationId} - ver docs/adr/ADR-013-pipeline-ingestao-normalizacao.md.
 */
class SimulationEndToEndIntegrationTest extends AbstractIntegrationTest {

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
    void authenticatedUserRunsAccountCompromisedScenarioAndAllEventsArriveNormalized() {
        TokenResponse tokens = login("synthetic-soc-analyst-01").getBody();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(tokens.accessToken());

        StartRunRequest request = new StartRunRequest(Scenario.ACCOUNT_COMPROMISED.id(), Characters.ROBERTO_NOGUEIRA.id(), 6);
        ResponseEntity<RunResponse> response = restTemplate.postForEntity(
                url("/api/v1/simulation/runs"), new HttpEntity<>(request, headers), RunResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        RunResponse run = response.getBody();
        assertThat(run.status()).isEqualTo("COMPLETED");
        assertThat(run.publishedEventCount()).isEqualTo(6);

        await().atMost(Duration.ofSeconds(20)).untilAsserted(() ->
                assertThat(mongoTemplate.count(
                        Query.query(Criteria.where("correlationId").is(run.correlationId())), "security_events"))
                        .isEqualTo(6));
    }

    private ResponseEntity<TokenResponse> login(String username) {
        return restTemplate.postForEntity(
                url("/api/v1/auth/login"), Map.of("username", username, "password", SYNTHETIC_PASSWORD),
                TokenResponse.class);
    }
}
