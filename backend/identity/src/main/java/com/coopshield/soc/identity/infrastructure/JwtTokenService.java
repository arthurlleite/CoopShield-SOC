package com.coopshield.soc.identity.infrastructure;

import com.coopshield.soc.identity.application.AccessToken;
import com.coopshield.soc.identity.application.AccessTokenIssuer;
import com.coopshield.soc.sharedkernel.identity.AuthenticatedPrincipal;
import com.coopshield.soc.sharedkernel.identity.Role;
import com.coopshield.soc.sharedkernel.identity.TokenValidator;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Emissao e validacao de access tokens JWT (HS256). Implementa duas
 * portas: {@link AccessTokenIssuer} (usada apenas dentro do modulo
 * identity) e {@link TokenValidator} (contrato compartilhado, consumido
 * pelo modulo accesscontrol).
 *
 * <p>Nunca registra o valor do token em log - apenas o resultado da
 * validacao (sucesso/falha) e, em caso de falha, a classe da excecao.
 */
@Component
public class JwtTokenService implements AccessTokenIssuer, TokenValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenService.class);
    private static final String CLAIM_USERNAME = "username";
    private static final String CLAIM_ROLE = "role";

    private final SecretKey signingKey;
    private final java.time.Duration accessTokenTtl;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtl = jwtProperties.getAccessTokenTtl();
    }

    @Override
    public AccessToken issue(UUID userId, String username, Role role) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = now.plus(accessTokenTtl);

        String jwt = Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_USERNAME, username)
                .claim(CLAIM_ROLE, role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();

        return new AccessToken(jwt, expiresAt);
    }

    @Override
    public Optional<AuthenticatedPrincipal> validateAccessToken(String rawToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(rawToken)
                    .getPayload();

            UUID userId = UUID.fromString(claims.getSubject());
            String username = claims.get(CLAIM_USERNAME, String.class);
            Role role = Role.valueOf(claims.get(CLAIM_ROLE, String.class));

            return Optional.of(new AuthenticatedPrincipal(userId, username, role));
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Access token rejected: {}", e.getClass().getSimpleName());
            return Optional.empty();
        }
    }
}
