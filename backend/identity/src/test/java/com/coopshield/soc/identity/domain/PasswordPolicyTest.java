package com.coopshield.soc.identity.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PasswordPolicyTest {

    @Test
    void acceptsStrongPassword() {
        assertThatCode(() -> PasswordPolicy.validate("synthetic-Passw0rd"))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsTooShortPassword() {
        assertThatThrownBy(() -> PasswordPolicy.validate("Ab1"))
                .isInstanceOf(WeakPasswordException.class);
    }

    @Test
    void rejectsPasswordWithoutDigit() {
        assertThatThrownBy(() -> PasswordPolicy.validate("onlylettershere"))
                .isInstanceOf(WeakPasswordException.class);
    }

    @Test
    void rejectsPasswordWithoutLetter() {
        assertThatThrownBy(() -> PasswordPolicy.validate("123456789012"))
                .isInstanceOf(WeakPasswordException.class);
    }

    @Test
    void rejectsNullPassword() {
        assertThatThrownBy(() -> PasswordPolicy.validate(null))
                .isInstanceOf(WeakPasswordException.class);
    }
}
