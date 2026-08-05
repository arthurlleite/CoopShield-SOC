package com.coopshield.soc.identity.domain;

/**
 * Lancada quando uma tentativa de login incide sobre uma conta atualmente
 * bloqueada por {@link AccountLockoutPolicy}. A camada de apresentacao
 * (controller) deve mapear esta excecao para a MESMA resposta HTTP
 * generica usada para {@link InvalidCredentialsException}, para nao
 * revelar a um observador externo se a conta existe e esta bloqueada.
 * A distincao fica registrada apenas na auditoria interna.
 */
public class AccountLockedException extends RuntimeException {

    public AccountLockedException() {
        super("Conta temporariamente bloqueada.");
    }
}
