package com.coopshield.soc.app;

import com.coopshield.soc.identity.infrastructure.web.TokenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prova as regras de autorizacao por perfil definidas em
 * {@link com.coopshield.soc.app.security.SecurityConfig}, mesmo sem ainda
 * existir nenhum controlador de negocio sob esses prefixos (chegam nas
 * fases 6 a 9). A distincao entre 403 (bloqueado pela regra de
 * autorizacao) e 404 (autorizado a passar, mas sem endpoint) prova que a
 * cadeia de seguranca aplica a regra correta, sem depender de
 * funcionalidade futura.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class RoleBasedAccessIntegrationTest {

    private static final String SYNTHETIC_PASSWORD = "Synthetic#Pass123";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    private String accessTokenFor(String username) {
        TokenResponse tokens = restTemplate.postForEntity(
                url("/api/v1/auth/login"),
                Map.of("username", username, "password", SYNTHETIC_PASSWORD),
                TokenResponse.class).getBody();
        return tokens.accessToken();
    }

    private ResponseEntity<Map> get(String path, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        if (accessToken != null) {
            headers.setBearerAuth(accessToken);
        }
        return restTemplate.exchange(url(path), HttpMethod.GET, new HttpEntity<>(headers), Map.class);
    }

    @Test
    void adminRouteRejectsUnauthenticatedRequests() {
        assertThat(get("/api/v1/admin/example", null).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void adminRouteAllowsItAdminThroughToTheMissingController() {
        String token = accessTokenFor("synthetic-it-admin-01");

        assertThat(get("/api/v1/admin/example", token).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void adminRouteDeniesOtherRoles() {
        String token = accessTokenFor("synthetic-soc-analyst-01");

        assertThat(get("/api/v1/admin/example", token).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void auditRouteAllowsAuditorThroughToTheMissingController() {
        String token = accessTokenFor("synthetic-auditor-01");

        assertThat(get("/api/v1/audit/example", token).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void auditRouteDeniesNonAuditorRoles() {
        String token = accessTokenFor("synthetic-employee-01");

        assertThat(get("/api/v1/audit/example", token).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void socRouteAllowsSocAnalystAndSocManager() {
        assertThat(get("/api/v1/soc/example", accessTokenFor("synthetic-soc-analyst-01")).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get("/api/v1/soc/example", accessTokenFor("synthetic-soc-manager-01")).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void socRouteDeniesOtherRoles() {
        String token = accessTokenFor("synthetic-branch-manager-01");

        assertThat(get("/api/v1/soc/example", token).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
