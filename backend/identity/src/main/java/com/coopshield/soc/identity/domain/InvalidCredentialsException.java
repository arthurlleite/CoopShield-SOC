package com.coopshield.soc.identity.domain;

/**
 * Lancada quando usuario/senha nao conferem, ou quando o usuario informado
 * nao existe. A mensagem e deliberadamente generica em ambos os casos,
 * para nao permitir enumeracao de usuarios validos.
 */
public class InvalidCredentialsException extends RuntimeException {

    public InvalidCredentialsException() {
        super("Usuario ou senha invalidos.");
    }
}
