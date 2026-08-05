package com.coopshield.soc.identity.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

/**
 * Aplica SHA-256 ao segredo de um refresh token antes da persistencia, de
 * forma que o repositorio nunca armazene um valor diretamente utilizavel
 * como token (defesa em profundidade, na linha de docs/adr/ADR-004-tokenizacao.md).
 * Nao e um algoritmo criptografico proprio: usa {@link MessageDigest} da
 * biblioteca padrao do Java.
 */
public final class RefreshTokenSecretHasher {

    private RefreshTokenSecretHasher() {
    }

    public static String hash(String secret) {
        Objects.requireNonNull(secret, "secret must not be null");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
