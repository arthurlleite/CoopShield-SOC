package com.coopshield.soc.identity.domain;

/**
 * Lancada quando um refresh token e invalido, expirado, revogado ou
 * malformado. A mensagem e generica para nao revelar qual dessas
 * condicoes se aplica.
 */
public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("Refresh token invalido.");
    }
}
