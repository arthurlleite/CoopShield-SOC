package com.coopshield.soc.identity.infrastructure;

import com.coopshield.soc.identity.application.AccessToken;
import com.coopshield.soc.sharedkernel.identity.AuthenticatedPrincipal;
import com.coopshield.soc.sharedkernel.identity.Role;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    private JwtTokenService newService(Duration ttl) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-only-secret-key-with-at-least-256-bits-of-entropy-1234567890");
        properties.setAccessTokenTtl(ttl);
        return new JwtTokenService(properties);
    }

    @Test
    void issuedTokenValidatesBackToTheSamePrincipal() {
        JwtTokenService service = newService(Duration.ofMinutes(15));
        UUID userId = UUID.randomUUID();

        AccessToken token = service.issue(userId, "synthetic-analyst-01", Role.SOC_ANALYST);
        Optional<AuthenticatedPrincipal> principal = service.validateAccessToken(token.value());

        assertThat(principal).isPresent();
        assertThat(principal.get().userId()).isEqualTo(userId);
        assertThat(principal.get().username()).isEqualTo("synthetic-analyst-01");
        assertThat(principal.get().role()).isEqualTo(Role.SOC_ANALYST);
    }

    @Test
    void rejectsExpiredToken() {
        // TTL negativo em vez de um TTL minusculo + sleep: o emissor trunca o
        // instante de emissao para segundos, entao uma margem de poucos
        // milissegundos seria sensivel ao ponto do segundo em que o teste
        // roda. Um TTL negativo garante expiracao no passado de forma
        // deterministica, sem depender de tempo real decorrido.
        JwtTokenService service = newService(Duration.ofSeconds(-2));
        AccessToken token = service.issue(UUID.randomUUID(), "synthetic-analyst-01", Role.SOC_ANALYST);

        assertThat(service.validateAccessToken(token.value())).isEmpty();
    }

    @Test
    void rejectsTamperedToken() {
        JwtTokenService service = newService(Duration.ofMinutes(15));
        AccessToken token = service.issue(UUID.randomUUID(), "synthetic-analyst-01", Role.SOC_ANALYST);
        String tampered = token.value().substring(0, token.value().length() - 2) + "xx";

        assertThat(service.validateAccessToken(tampered)).isEmpty();
    }

    @Test
    void rejectsGarbageToken() {
        JwtTokenService service = newService(Duration.ofMinutes(15));

        assertThat(service.validateAccessToken("not-a-jwt")).isEmpty();
    }

    @Test
    void signatureFromADifferentSecretIsRejected() {
        JwtTokenService issuer = newService(Duration.ofMinutes(15));
        JwtProperties otherProperties = new JwtProperties();
        otherProperties.setSecret("a-completely-different-secret-key-with-enough-entropy-abcdefgh");
        JwtTokenService validator = new JwtTokenService(otherProperties);

        AccessToken token = issuer.issue(UUID.randomUUID(), "synthetic-analyst-01", Role.SOC_ANALYST);

        assertThat(validator.validateAccessToken(token.value())).isEmpty();
    }

    @Test
    void expiresAtMatchesRequestedTtl() {
        JwtTokenService service = newService(Duration.ofMinutes(15));
        Instant before = Instant.now();

        AccessToken token = service.issue(UUID.randomUUID(), "synthetic-analyst-01", Role.SOC_ANALYST);

        assertThat(token.expiresAt()).isAfter(before.plus(Duration.ofMinutes(14)));
        assertThat(token.expiresAt()).isBefore(before.plus(Duration.ofMinutes(16)));
    }
}
