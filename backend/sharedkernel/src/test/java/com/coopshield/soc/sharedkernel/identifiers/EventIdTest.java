package com.coopshield.soc.sharedkernel.identifiers;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventIdTest {

    @Test
    void newIdGeneratesUniqueValues() {
        EventId first = EventId.newId();
        EventId second = EventId.newId();

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void ofParsesValidUuidString() {
        EventId id = EventId.of("2c7f2f0e-8f8d-4e8b-9f6a-8f2b6c7a1234");

        assertThat(id.toString()).isEqualTo("2c7f2f0e-8f8d-4e8b-9f6a-8f2b6c7a1234");
    }

    @Test
    void rejectsNullValue() {
        assertThatThrownBy(() -> new EventId(null))
                .isInstanceOf(NullPointerException.class);
    }
}
