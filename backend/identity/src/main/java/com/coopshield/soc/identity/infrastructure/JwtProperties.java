package com.coopshield.soc.identity.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuracao do emissor/validador de access tokens (JWT). O valor padrao
 * de {@code secret} serve apenas para execucao local/demonstracao com
 * dados sinteticos; qualquer uso além disso deve sobrescrever
 * {@code coopshield.security.jwt.secret} via variavel de ambiente,
 * nunca no repositorio (ver docs/adr/ADR-004-tokenizacao.md).
 */
@ConfigurationProperties(prefix = "coopshield.security.jwt")
public class JwtProperties {

    private String secret = "local-only-synthetic-development-secret-key-not-for-production-use";
    private Duration accessTokenTtl = Duration.ofMinutes(15);

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public void setAccessTokenTtl(Duration accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }
}
