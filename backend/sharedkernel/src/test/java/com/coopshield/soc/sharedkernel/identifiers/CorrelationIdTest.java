package com.coopshield.soc.sharedkernel.identifiers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CorrelationIdTest {

    @Test
    void newIdGeneratesUniqueValues() {
        CorrelationId first = CorrelationId.newId();
        CorrelationId second = CorrelationId.newId();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void sameUnderlyingValueIsEqual() {
        CorrelationId id = CorrelationId.newId();
        CorrelationId sameValue = CorrelationId.of(id.value().toString());

        assertThat(id).isEqualTo(sameValue);
    }

    @Test
    void rejectsNullValue() {
        assertThatThrownBy(() -> new CorrelationId(null))
                .isInstanceOf(NullPointerException.class);
    }
}
