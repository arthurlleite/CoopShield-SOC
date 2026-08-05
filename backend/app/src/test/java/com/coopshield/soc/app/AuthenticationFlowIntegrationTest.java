package com.coopshield.soc.app;

import com.coopshield.soc.identity.infrastructure.web.TokenResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class AuthenticationFlowIntegrationTest {

    private static final String SYNTHETIC_PASSWORD = "Synthetic#Pass123";

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String url(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void loginWithValidCredentialsReturnsTokens() {
        ResponseEntity<TokenResponse> response = login("synthetic-soc-analyst-01", SYNTHETIC_PASSWORD);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().accessToken()).isNotBlank();
        assertThat(response.getBody().refreshToken()).contains(".");
    }

    @Test
    void loginWithWrongPasswordReturnsGenericUnauthorized() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/login"),
                Map.of("username", "synthetic-employee-01", "password", "wrong-password"),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("error", "invalid_credentials");
    }

    @Test
    void loginWithUnknownUserReturnsTheSameGenericUnauthorized() {
        ResponseEntity<Map> response = restTemplate.postForEntity(
                url("/api/v1/auth/login"),
                Map.of("username", "does-not-exist", "password", "whatever12"),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("error", "invalid_credentials");
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    void accountLocksAfterRepeatedFailuresAndStaysLockedEvenWithCorrectPassword() {
        String username = "synthetic-branch-manager-01";

        for (int i = 0; i < 5; i++) {
            ResponseEntity<Map> failed = restTemplate.postForEntity(
                    url("/api/v1/auth/login"),
                    Map.of("username", username, "password", "wrong-password"),
                    Map.class);
            assertThat(failed.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        ResponseEntity<Map> lockedButCorrectPassword = restTemplate.postForEntity(
                url("/api/v1/auth/login"),
                Map.of("username", username, "password", SYNTHETIC_PASSWORD),
                Map.class);

        // Mesma resposta generica de credenciais invalidas - o bloqueio nao e
        // revelado externamente, apenas via auditoria interna.
        assertThat(lockedButCorrectPassword.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(lockedButCorrectPassword.getBody()).containsEntry("error", "invalid_credentials");
    }

    @Test
    void refreshRotatesTokenAndOldRefreshTokenCannotBeReused() {
        TokenResponse first = login("synthetic-it-admin-01", SYNTHETIC_PASSWORD).getBody();

        ResponseEntity<TokenResponse> refreshed = restTemplate.postForEntity(
                url("/api/v1/auth/refresh"), Map.of("refreshToken", first.refreshToken()), TokenResponse.class);
        assertThat(refreshed.getStatusCode()).isEqualTo(HttpStatus.OK);
        // O refresh token e sempre unico (UUID + segredo aleatorio); o access
        // token pode colidir em valor se emitido no mesmo segundo com claims
        // identicas, entao a rotacao e verificada pelo refresh token, nao pelo
        // access token.
        assertThat(refreshed.getBody().refreshToken()).isNotEqualTo(first.refreshToken());

        ResponseEntity<Map> reuseOldToken = restTemplate.postForEntity(
                url("/api/v1/auth/refresh"), Map.of("refreshToken", first.refreshToken()), Map.class);
        assertThat(reuseOldToken.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void logoutRevokesRefreshTokenSoItCanNoLongerBeUsed() {
        TokenResponse tokens = login("synthetic-auditor-01", SYNTHETIC_PASSWORD).getBody();

        ResponseEntity<Void> logoutResponse = restTemplate.postForEntity(
                url("/api/v1/auth/logout"), Map.of("refreshToken", tokens.refreshToken()), Void.class);
        assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<Map> refreshAfterLogout = restTemplate.postForEntity(
                url("/api/v1/auth/refresh"), Map.of("refreshToken", tokens.refreshToken()), Map.class);
        assertThat(refreshAfterLogout.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void meEndpointRequiresAuthentication() {
        ResponseEntity<Map> response = restTemplate.getForEntity(url("/api/v1/me"), Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void meEndpointReturnsAuthenticatedIdentityWithValidToken() {
        TokenResponse tokens = login("synthetic-soc-manager-01", SYNTHETIC_PASSWORD).getBody();

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setBearerAuth(tokens.accessToken());
        ResponseEntity<Map> response = restTemplate.exchange(
                url("/api/v1/me"),
                org.springframework.http.HttpMethod.GET,
                new org.springframework.http.HttpEntity<>(headers),
                Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("username", "synthetic-soc-manager-01");
        assertThat(response.getBody()).containsEntry("role", "SOC_MANAGER");
    }

    private ResponseEntity<TokenResponse> login(String username, String password) {
        return restTemplate.postForEntity(
                url("/api/v1/auth/login"), Map.of("username", username, "password", password), TokenResponse.class);
    }
}
