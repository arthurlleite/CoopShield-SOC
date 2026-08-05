package com.coopshield.soc.sharedkernel.identity;

import java.util.Optional;

/**
 * Porta de validacao de token de acesso. Implementada pelo modulo identity
 * (JWT); consumida pelo modulo accesscontrol para autenticar requisicoes,
 * sem que accesscontrol conheca o formato ou a biblioteca de token usada.
 */
public interface TokenValidator {

    /**
     * Valida um token de acesso bruto (ex.: o valor do header
     * {@code Authorization: Bearer <token>}, sem o prefixo).
     *
     * @return a identidade autenticada, ou {@link Optional#empty()} se o
     *         token for invalido, expirado ou malformado.
     */
    Optional<AuthenticatedPrincipal> validateAccessToken(String rawToken);
}
