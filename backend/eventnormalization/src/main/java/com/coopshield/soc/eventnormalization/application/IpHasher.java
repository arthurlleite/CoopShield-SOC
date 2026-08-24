package com.coopshield.soc.eventnormalization.application;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Objects;

/**
 * Aplica SHA-256 ao endereco IP de origem de um evento bruto antes que ele
 * possa se tornar um {@link com.coopshield.soc.sharedkernel.event.NetworkContext},
 * cujo construtor proibe explicitamente um IP em texto puro. Mesmo padrao de
 * {@code RefreshTokenSecretHasher} (modulo identity): sem algoritmo
 * criptografico proprio, apenas {@link MessageDigest} da biblioteca padrao.
 */
public final class IpHasher {

    private IpHasher() {
    }

    public static String hash(String sourceIp) {
        Objects.requireNonNull(sourceIp, "sourceIp must not be null");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(sourceIp.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
