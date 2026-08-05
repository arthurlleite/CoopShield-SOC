package com.coopshield.soc.identity.domain;

import java.util.regex.Pattern;

/**
 * Politica minima de senha para usuarios sinteticos do CoopShield SOC:
 * comprimento minimo e presenca de letra e digito. Aplicada na criacao de
 * usuarios (ex.: {@code SyntheticUserSeeder}); nao se aplica a tentativas
 * de login, apenas a definicao/alteracao de senha.
 */
public final class PasswordPolicy {

    private static final int MIN_LENGTH = 12;
    private static final Pattern HAS_LETTER = Pattern.compile("[A-Za-z]");
    private static final Pattern HAS_DIGIT = Pattern.compile("[0-9]");

    private PasswordPolicy() {
    }

    public static void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.length() < MIN_LENGTH) {
            throw new WeakPasswordException("A senha deve ter ao menos " + MIN_LENGTH + " caracteres.");
        }
        if (!HAS_LETTER.matcher(rawPassword).find() || !HAS_DIGIT.matcher(rawPassword).find()) {
            throw new WeakPasswordException("A senha deve conter ao menos uma letra e um digito.");
        }
    }
}
