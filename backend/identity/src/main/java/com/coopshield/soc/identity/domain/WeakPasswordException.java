package com.coopshield.soc.identity.domain;

/**
 * Lancada quando uma senha em texto puro nao atende a
 * {@link PasswordPolicy}. Nunca deve ser tratada de forma a expor a senha
 * em log ou mensagem de erro.
 */
public class WeakPasswordException extends RuntimeException {

    public WeakPasswordException(String message) {
        super(message);
    }
}
